package com.huliua.common.utils;

import com.huliua.common.domain.ResponseResult;
import org.springframework.http.HttpStatus;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-22 09:27
 */
public class ResponseUtil {

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(HttpStatus.OK.value(), data, "操作成功");
    }

    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(HttpStatus.OK.value(), null, "操作成功");
    }

    public static <T> ResponseResult<T> fail() {
        return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "操作失败");
    }

    public static <T> ResponseResult<T> fail(String msg) {
        return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, msg);
    }
}
