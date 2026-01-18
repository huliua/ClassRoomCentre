package com.huliua.classroomcentre.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.dto.ClassRoomExportDto;
import com.huliua.classroomcentre.domain.entity.ClassRoom;
import com.huliua.classroomcentre.domain.mapper.ClassRoomBeanMapper;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import com.huliua.classroomcentre.mapper.ClassRoomMapper;
import com.huliua.classroomcentre.mapper.ClassRoomOccupyMapper;
import com.huliua.classroomcentre.service.ClassRoomService;
import com.huliua.common.domain.BusinessException;
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 16:55
 */
@Service
@Slf4j
@AllArgsConstructor
public class ClassRoomServiceImpl extends ServiceImpl<ClassRoomMapper, ClassRoom> implements ClassRoomService {

    private final ClassRoomBeanMapper classRoomBeanMapper;
    private final ClassRoomOccupyMapper classRoomOccupyMapper;
    private final ClassRoomMapper classRoomMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private DefaultRedisScript<Long> occupyScript;

    @Override
    public PageResult<ClassRoomVo> pageQuery(ClassRoomDto classRoomDto) {
        IPage<ClassRoom> page = new Page<>(classRoomDto.getPageNum(), classRoomDto.getPageSize());
        IPage<ClassRoomVo> pageRes = classRoomMapper.pageQuery(page, classRoomDto.buildQueryWrapper());
        return new PageResult<>(pageRes.getTotal(), pageRes.getCurrent(), pageRes.getSize(), pageRes.getPages(), pageRes.getRecords());
    }

    @Override
    public ResponseResult<Void> occupy(Long classroomId) {
        try {
            String key = "classroom:capacity:" + classroomId;

            // 1. 执行脚本
            long result = redisTemplate.execute(occupyScript, Collections.singletonList(key));
            // 2. 判断结果
            if (result == -1) {
                return ResponseUtil.fail("当前活动未开始！");
            }
            if (result == 0) {
                return ResponseUtil.fail("教室已满！");
            }
            // 3. Redis扣减成功，发送 MQ (此时已获得资格)
            try {
                Map<String, Object> message = new HashMap<>();
                message.put("classroomId", classroomId);
                message.put("msgId", UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("classroom-centre-exchange", "classroom.occupy", message);
            } catch (Exception mqEx) {
                // 4. 【关键】MQ 发送失败的补偿机制
                log.error("MQ send failed, rolling back Redis capacity", mqEx);
                // 回滚 Redis 库存
                redisTemplate.opsForValue().increment(key);
                return ResponseUtil.fail("网络抖动，请重试");
            }
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("occupy error:", e);
            return ResponseUtil.fail("服务繁忙，请稍后再试");
        }
    }

    @Override
    public ResponseResult<String> initCache() {
        try {
            // 先初始化各个教室的剩余容量信息（只初始化1000条测试数据）
            ClassRoomDto classRoomDto = new ClassRoomDto();
            classRoomDto.setPageSize(1000);
            PageResult<ClassRoomVo> pageResult = this.pageQuery(classRoomDto);
            List<ClassRoomVo> classRoomVos = pageResult.getRows();

            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                classRoomVos.forEach(vo -> {
                    String key = "classroom:capacity:" + vo.getId();
                    connection.stringCommands().set(key.getBytes(), String.valueOf(vo.getLeftCount()).getBytes());
                });
                return null;
            });

            // 检查是否有操作失败
            if (results.contains(null)) {
                log.warn("部分缓存初始化失败");
                return ResponseUtil.fail("部分缓存初始化失败！");
            }
        } catch (Exception e) {
            log.error("初始化缓存失败！", e);
            return ResponseUtil.fail("初始化缓存失败！");
        }
        return ResponseUtil.success("初始化缓存成功！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doOccupy(String classroomId, String userId) {
        // 添加占用关系
        int count = classRoomOccupyMapper.addIfHasCapacity(Long.valueOf(classroomId), userId);
        if (count <= 0) {
            // 添加失败，则抛出异常，回滚事务
            throw new BusinessException(-1, "添加失败，教室已满！");
        }
    }

    /**
     * 使用easyExcel下载教室信息
     *
     * @param classRoomDto 查询条件
     */
    @Override
    public void download(ClassRoomDto classRoomDto) {
        // 计时
        StopWatch sw = new StopWatch();
        sw.start("导出数据测试");

        // 获取总页数
        long count = this.count(classRoomDto.buildQueryWrapper());
        long pageSize = 5000;
        // 计算总页数
        long pageCount = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hhmmss");
        String fileName = "/Users/tigerl/Coder/Java/ClassRoomCentre/" + LocalDateTime.now().format(formatter) + ".xlsx";
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors() * 2, 8));
        List<CompletableFuture<List<ClassRoom>>> futures = new ArrayList<>();
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, ClassRoomExportDto.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("教室数据").build();
            // 循环写入数据，每次循环只写入pageSize条数据
            for (long i = 0; i < pageCount; i++) {
                long finalI = i;
                CompletableFuture<List<ClassRoom>> f = CompletableFuture.supplyAsync(() -> {
                    // 构建分区查询对象
                    Page<ClassRoom> page = new Page<>(finalI + 1, (int) pageSize);
                    // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                    Page<ClassRoom> paged = this.page(page, classRoomDto.buildQueryWrapper());
                    return paged.getRecords();
                }, executorService);
                futures.add(f);
            }

            // 按顺序获取并写入数据
            for (int i = 0; i < futures.size(); i++) {
                try {
                    List<ClassRoom> classRooms = futures.get(i).get(30, TimeUnit.SECONDS);
                    if (!classRooms.isEmpty()) {
                        excelWriter.write(classRooms, writeSheet);
                    }
                } catch (Exception e) {
                    log.error("处理第{}页数据失败", i + 1, e);
                }
            }
        } catch (Exception e) {
            log.error("导出数据失败！", e);
        } finally {
            // 优雅关闭线程池
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.error("线程池无法正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        sw.stop();
        // 输出日志
        log.info("导出数据测试耗时：{}ms", sw.getTotalTimeMillis());
    }

    /**
     * 从Excel导入数据 百万数据大概63秒左右
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFormExcel() {
        // 计时
        StopWatch sw = new StopWatch();
        sw.start("单线程导出数据测试");


        final Integer[] count = {0};
        String fileName = "/Users/tigerl/Coder/Java/ClassRoomCentre/data.xlsx";
        EasyExcel.read(fileName, ClassRoomExportDto.class, new ReadListener<ClassRoomExportDto>() {
            /**
             * 单次缓存的数据量
             */
            public static final int BATCH_COUNT = 100;
            /**
             *临时存储
             */
            private List<ClassRoomExportDto> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

            @Override
            public void invoke(ClassRoomExportDto data, AnalysisContext context) {
                cachedDataList.add(data);
                count[0]++;
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            /**
             * 加上存储数据库
             */
            private void saveData() {
                log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                saveBatch(classRoomBeanMapper.toClassRoomFormExport(cachedDataList));
            }
        }).sheet().doRead();

        sw.stop();
        // 输出日志
        log.info("单线程导入数据测试耗时：{}ms，总数据量：{}条", sw.getTotalTimeMillis(), count[0]);
        return count[0];
    }


    /**
     * 从Excel导入数据--多线程方式 百万数据大概33秒左右
     * 另外需要考虑多线程下导入数据失败，数据一致性问题。
     * 例如：可以先将数据导入到临时表，然后通过SQL语句将临时表数据导入到正式表。如果导入到临时表中的数据插入不完整，则需要处理。
     */
    @Override
    public int parallelImportFormExcel() {
        // 计时
        StopWatch sw = new StopWatch();
        sw.start("单线程导出数据测试");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors() * 2, 8));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try {
            String fileName = "/Users/tigerl/Coder/Java/ClassRoomCentre/data.xlsx";
            EasyExcel.read(fileName, ClassRoom.class, new PageReadListener<ClassRoom>(data -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        boolean b = saveBatch(data, 1000);
                        if (b) {
                            successCount.addAndGet(data.size());
                        } else {
                            failCount.addAndGet(data.size());
                        }
                    } catch (Exception e) {
                        log.error("多线程导入数据失败！", e);
                        failCount.addAndGet(data.size());
                    }
                }, executorService);
                futures.add(future);
            }, 10000)).sheet().doRead();

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("多线程导入数据失败！", e);
        } finally {
            executorService.shutdown();
        }

        sw.stop();
        // 输出日志
        log.info("多线程导入数据测试耗时：{}ms，数据量：{},失败数量：{}", sw.getTotalTimeMillis(), successCount, failCount);
        return successCount.get();
    }
}
