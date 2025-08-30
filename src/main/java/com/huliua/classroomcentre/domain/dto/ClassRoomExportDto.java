package com.huliua.classroomcentre.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ClassRoomExportDto {

    @ExcelProperty("教室id")
    private Long id;

    @ExcelProperty("教室代码")
    private Long code;

    @ExcelProperty("教室名称")
    private String name;

    @ExcelProperty("教室容量")
    private Integer capacity;
}
