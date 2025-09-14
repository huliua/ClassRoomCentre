package com.huliua.classroomcentre.controller;

import com.huliua.classroomcentre.service.MqService;
import com.huliua.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
@Tag(name = "Mq相关接口", description = "操作mq的相关接口")
public class MqController {

    @Resource
    private MqService mqService;

    @PostMapping("/send.do")
    public ResponseResult<String> send(String msg) {
        return mqService.send(msg);
    }

    @PostMapping("/sendDelay.do")
    public ResponseResult<String> sendDelay(String msg, String ttl) {
        return mqService.sendDelay(msg, ttl);
    }
}
