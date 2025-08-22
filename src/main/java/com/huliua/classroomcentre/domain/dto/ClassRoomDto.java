package com.huliua.classroomcentre.domain.dto;

import com.huliua.common.domain.BasePageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 17:00
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoomDto extends BasePageQuery {

    private Long id;

    private Long code;

    private String name;

    private Integer capacity;
}
