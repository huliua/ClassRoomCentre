package com.huliua.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-22 09:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;
    private List<T> rows;
}
