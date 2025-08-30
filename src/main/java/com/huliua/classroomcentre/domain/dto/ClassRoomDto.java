package com.huliua.classroomcentre.domain.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huliua.classroomcentre.domain.entity.ClassRoom;
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

    public QueryWrapper<ClassRoom> getQueryWrapper() {
        QueryWrapper<ClassRoom> queryWrapper = new QueryWrapper<>();
        // 添加具体的查询条件
        if (this.getName() != null && !this.getName().isEmpty()) {
            queryWrapper.like("name", this.getName());
        }
        if (this.getCode() != null) {
            queryWrapper.eq("code", this.getCode());
        }
        return queryWrapper;
    }
}
