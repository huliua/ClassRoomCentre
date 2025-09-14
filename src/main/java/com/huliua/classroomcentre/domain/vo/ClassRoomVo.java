package com.huliua.classroomcentre.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教室信息VO")
public class ClassRoomVo {
    
    @Schema(description = "教室ID")
    private Long id;
    
    @Schema(description = "教室编码")
    private String code;
    
    @Schema(description = "教室名称")
    private String name;
    
    @Schema(description = "教室容量")
    private Integer capacity;
    
    @Schema(description = "已占用数量")
    private Integer occupyCount;
    
    @Schema(description = "剩余数量")
    private Integer leftCount;
}