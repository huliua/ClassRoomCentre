package com.huliua.classroomcentre.service.impl;

import com.huliua.classroomcentre.service.MqService;
import com.huliua.common.domain.ResponseResult;
import com.huliua.common.utils.ResponseUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqServiceImpl implements MqService {

    private final RabbitTemplate rabbitTemplate;

    MqServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public ResponseResult<String> send(String msg) {
        rabbitTemplate.convertAndSend("classroom-centre-exchange", "classroom.occupy", msg);
        return ResponseUtil.success();
    }

    @Override
    public ResponseResult<String> sendDelay(String msg, String ttl) {
        Message message = new Message(msg.getBytes(), new MessageProperties());
        message.getMessageProperties().setExpiration(ttl);
        rabbitTemplate.convertAndSend("classroom-centre-exchange", "delay", message);
        return ResponseUtil.success();
    }
}
