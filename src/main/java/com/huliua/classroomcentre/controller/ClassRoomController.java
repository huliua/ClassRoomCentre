package com.huliua.classroomcentre.controller;

import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import com.huliua.classroomcentre.service.ClassRoomService;
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "教室管理接口", description = "教室相关操作接口")
public class ClassRoomController {

    @Resource
    private ClassRoomService classRoomService;

    @GetMapping("/list.do")
    @Operation(summary = "分页查询教室信息", description = "分页查询教室信息列表")
    public ResponseResult<PageResult<ClassRoomVo>> list(
            @Parameter(description = "教室查询参数") ClassRoomDto classRoomDto) {
        PageResult<ClassRoomVo> dataList = classRoomService.pageQuery(classRoomDto);
        return ResponseUtil.success(dataList);
    }

    @PostMapping("/occupy.do")
    @Operation(summary = "占用教室容量", description = "占用指定教室的容量")
    public ResponseResult<Void> occupy(
            @Parameter(description = "教室ID", required = true) Long classroomId) {
        return classRoomService.occupy(classroomId);
    }

    @GetMapping("/download.do")
    @Operation(summary = "导出教室信息", description = "导出教室信息到Excel文件")
    public ResponseResult<String> download(
            @Parameter(description = "教室查询参数") ClassRoomDto classRoomDto) {
        classRoomService.download(classRoomDto);
        return ResponseUtil.success();
    }

    @GetMapping("/import.do")
    @Operation(summary = "导入教室信息", description = "从Excel文件导入教室信息")
    public ResponseResult<String> importFormExcel() {
        int count = classRoomService.importFormExcel();
        return ResponseUtil.success("成功导入"+ count + "条数据！");
    }

    @GetMapping("/import2.do")
    @Operation(summary = "并行导入教室信息", description = "从Excel文件并行导入教室信息")
    public ResponseResult<String> parallelImportFormExcel() {
        int count = classRoomService.parallelImportFormExcel();
        return ResponseUtil.success("成功导入"+ count + "条数据！");
    }

    @PostMapping("/initCache.do")
    @Operation(summary = "初始化缓存", description = "初始化缓存")
    public ResponseResult<String> initCache() {
        return classRoomService.initCache();
    }
}