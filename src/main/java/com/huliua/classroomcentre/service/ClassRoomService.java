package com.huliua.classroomcentre.service;

import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import com.huliua.common.domain.PageResult;
import com.huliua.common.domain.ResponseResult;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 16:55
 */
public interface ClassRoomService {
    PageResult<ClassRoomVo> pageQuery(ClassRoomDto classRoomDto);

    ResponseResult<Void> occupy(Long classroomId);

    void download(ClassRoomDto classRoomDto);

    int importFormExcel();

    int parallelImportFormExcel();
}
