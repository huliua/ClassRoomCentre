package com.huliua.classroomcentre.domain.mapper;

import com.huliua.classroomcentre.domain.dto.ClassRoomDto;
import com.huliua.classroomcentre.domain.dto.ClassRoomExportDto;
import com.huliua.classroomcentre.domain.entity.ClassRoom;
import com.huliua.classroomcentre.domain.vo.ClassRoomVo;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 18:57
 */
@Mapper(componentModel = "spring")
public interface ClassRoomBeanMapper {

    ClassRoomVo toClassRoomVo(ClassRoom classRoom);

    List<ClassRoomVo> toClassRoomVos(List<ClassRoom> classRooms);

    ClassRoom toClassRoom(ClassRoomDto classRoomDto);

    List<ClassRoom> toClassRooms(List<ClassRoomDto> classRoomDtos);

    List<ClassRoom> toClassRoomFormExport(List<ClassRoomExportDto> classRoomExportDto);
}
