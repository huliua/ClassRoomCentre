package com.huliua.classroomcentre.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.huliua.classroomcentre.domain.entity.ClassRoom;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import org.apache.ibatis.annotations.Param;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 16:57
 */
public interface ClassRoomMapper extends BaseMapper<ClassRoom> {

    IPage<ClassRoomVo> pageQuery(@Param("page") IPage<ClassRoom> page, @Param(Constants.WRAPPER)QueryWrapper<ClassRoom> queryWrapper);
}
