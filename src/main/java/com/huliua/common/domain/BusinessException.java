package com.huliua.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * BusinessException
 *
 * @author tigerl
 * @version 1.0
 * @datetime 2026/1/18 17:33
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException {

    private Integer code;

    private String message;
}
