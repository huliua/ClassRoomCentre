package com.huliua.classroomcentre.domain.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huliua.classroomcentre.domain.entity.ClassRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教室查询参数")
public class ClassRoomDto {
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "教室编码")
    private String code;
    
    @Schema(description = "教室名称")
    private String name;
    
    public QueryWrapper<ClassRoom> buildQueryWrapper() {
        QueryWrapper<ClassRoom> queryWrapper = new QueryWrapper<>();
        if (code != null && !code.isEmpty()) {
            queryWrapper.eq("code", code);
        }
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        return queryWrapper;
    }
}
