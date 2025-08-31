package com.huliua.classroomcentre.domain.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 16:51
 */
@TableName("t_classroom")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoom {

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.ASSIGN_ID)
    @ExcelProperty("教室id")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ExcelProperty("教室代码")
    private Long code;

    @ExcelProperty("教室名称")
    private String name;

    @ExcelProperty("教室容量")
    private Integer capacity;
}
