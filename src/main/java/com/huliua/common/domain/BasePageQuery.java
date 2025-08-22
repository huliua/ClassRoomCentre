package com.huliua.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-21 17:02
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasePageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderBy;
}
