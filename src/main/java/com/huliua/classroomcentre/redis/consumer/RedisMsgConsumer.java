package com.huliua.classroomcentre.redis.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;

@Repository
@Slf4j
public class RedisMsgConsumer implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 处理接收到的消息
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received message from channel [{}]: {}", channel, body);
    }
}