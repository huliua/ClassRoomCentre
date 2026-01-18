package com.huliua.classroomcentre.controller;

import com.huliua.classroomcentre.config.RedisConfig;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisMsgController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("/sendMsg")
    public String sendMsg(String msg) {
        redisTemplate.convertAndSend(RedisConfig.CHANNEL_TOPIC, msg);
        return "success";
    }

}
