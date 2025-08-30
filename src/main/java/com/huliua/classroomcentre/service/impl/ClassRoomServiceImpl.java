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
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
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
public class ClassRoomServiceImpl extends ServiceImpl<ClassRoomMapper, ClassRoom> implements ClassRoomService {

    @Resource
    private ClassRoomBeanMapper classRoomBeanMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ClassRoomOccupyMapper classRoomOccupyMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResult<ClassRoomVo> pageQuery(ClassRoomDto classRoomDto) {
        IPage<ClassRoom> page = new Page<>(classRoomDto.getPageNum(), classRoomDto.getPageSize());

        IPage<ClassRoom> pageRes = this.page(page, classRoomDto.getQueryWrapper());
        return new PageResult<>(pageRes.getTotal(), pageRes.getCurrent(), pageRes.getSize(), pageRes.getPages(), classRoomBeanMapper.toClassRoomVos(pageRes.getRecords()));
    }

    @Override
    public ResponseResult<Void> occupy(Long classroomId) {
        RLock lock = redissonClient.getLock("classroom:occupy:" + classroomId);
        try {
            boolean locked = lock.tryLock(5, 60, TimeUnit.SECONDS);
            if (!locked) {
                return ResponseUtil.fail("服务繁忙，请稍后再试！");
            }
            // 如果容量还有剩余，就占用教室1个容量
            int count = classRoomOccupyMapper.addIfHasCapacity(classroomId);
            if (count == 0) {
                return ResponseUtil.fail("教室容量已满！");
            }
            rabbitTemplate.convertAndSend("classroom-centre-exchange", "classroom.occupy", "教室" + classroomId + "新增占用", new CorrelationData(classroomId + ""));
            return ResponseUtil.success();
        } catch (InterruptedException e) {
            log.error("获取锁失败！", e);
            return ResponseUtil.fail("服务繁忙，请稍后再试！");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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
        sw.start("单线程导出数据测试");

        // 获取总页数
        long count = this.count(classRoomDto.getQueryWrapper());
        long pageSize = 5000;
        // 计算总页数
        long pageCount = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        Page<ClassRoom> page = null;
        Page<ClassRoom> pageData = null;

        String fileName = "/Users/tigerl/Coder/Java/ClassRoomCentre/" + System.currentTimeMillis() + ".xlsx";
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, ClassRoomExportDto.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("教室数据").build();
            // 循环写入数据，每次循环只写入pageSize条数据
            for (long i = 0; i < pageCount; i++) {
                // 构建分区查询对象
                page = new Page<>(i + 1, (int) pageSize);
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                pageData = this.page(page, classRoomDto.getQueryWrapper());
                excelWriter.write(pageData.getRecords(), writeSheet);
            }
        }
        sw.stop();

        // 输出日志
        log.info("单线程导出数据测试耗时：{}ms", sw.getTotalTimeMillis());

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
     *     例如：可以先将数据导入到临时表，然后通过SQL语句将临时表数据导入到正式表。如果导入到临时表中的数据插入不完整，则需要处理。
     */
    @Override
    public int parallelImportFormExcel() {
        // 计时
        StopWatch sw = new StopWatch();
        sw.start("单线程导出数据测试");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool( Math.min(Runtime.getRuntime().availableProcessors() * 2, 8));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try {
            String fileName = "/Users/tigerl/Coder/Java/ClassRoomCentre/data.xlsx";
            EasyExcel.read(fileName, ClassRoomExportDto.class, new PageReadListener<ClassRoomExportDto>(data -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        boolean b = saveBatch(classRoomBeanMapper.toClassRoomFormExport(data), 1000);
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
