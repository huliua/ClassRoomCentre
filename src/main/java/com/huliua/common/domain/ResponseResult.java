package com.huliua.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-22 09:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {

    private int code;

    private T data;

    private String message;
}
