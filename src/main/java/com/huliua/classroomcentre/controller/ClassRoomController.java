package com.huliua.classroomcentre.controller;

import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import com.huliua.classroomcentre.service.ClassRoomService;
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 16:57
 */
@RestController
@RequestMapping("/classroom")
public class ClassRoomController {

    @Resource
    private ClassRoomService classRoomService;

    @GetMapping("/list.do")
    public ResponseResult<PageResult<ClassRoomVo>> list(ClassRoomDto classRoomDto) {
        PageResult<ClassRoomVo> dataList = classRoomService.pageQuery(classRoomDto);
        return ResponseUtil.success(dataList);
    }

    @GetMapping("/occupy.do")
    public ResponseResult<Void> occupy(Long classroomId) {
        return classRoomService.occupy(classroomId);
    }
}
