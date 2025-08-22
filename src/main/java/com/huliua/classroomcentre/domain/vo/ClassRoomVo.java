package com.huliua.classroomcentre.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 17:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoomVo {

    private Long id;

    private Long code;

    private String name;

    private Integer capacity;
}