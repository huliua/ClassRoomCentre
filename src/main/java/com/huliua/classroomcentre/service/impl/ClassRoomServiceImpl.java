package com.huliua.classroomcentre.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
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

import java.util.concurrent.TimeUnit;

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
        QueryWrapper<ClassRoom> queryWrapper = new QueryWrapper<>();
        // 添加具体的查询条件
        if (classRoomDto.getName() != null && !classRoomDto.getName().isEmpty()) {
            queryWrapper.like("name", classRoomDto.getName());
        }
        if (classRoomDto.getCode() != null) {
            queryWrapper.eq("code", classRoomDto.getCode());
        }
        IPage<ClassRoom> pageRes = this.page(page, queryWrapper);
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
}
