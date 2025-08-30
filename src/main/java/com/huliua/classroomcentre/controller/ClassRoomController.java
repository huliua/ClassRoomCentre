package com.huliua.classroomcentre.controller;

import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import com.huliua.classroomcentre.service.ClassRoomService;
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/occupy.do")
    public ResponseResult<Void> occupy(Long classroomId) {
        return classRoomService.occupy(classroomId);
    }

    @GetMapping("/download.do")
    public ResponseResult<String> download(ClassRoomDto classRoomDto) {
        classRoomService.download(classRoomDto);
        return ResponseUtil.success();
    }

    @GetMapping("/import.do")
    public ResponseResult<String> importFormExcel() {
        int count = classRoomService.importFormExcel();
        return ResponseUtil.success("成功导入"+ count + "条数据！");
    }

    @GetMapping("/import2.do")
    public ResponseResult<String> parallelImportFormExcel() {
        int count = classRoomService.parallelImportFormExcel();
        return ResponseUtil.success("成功导入"+ count + "条数据！");
    }
}
