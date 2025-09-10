package com.huliua.classroomcentre.service;

import com.huliua.common.domain.ResponseResult;

public interface MqService {
    ResponseResult<String> send(String msg);

    ResponseResult<String> sendDelay(String msg, String ttl);
}
