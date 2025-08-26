package com.huliua.classroomcentre.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class TestConsumer {

    @RabbitListener(queues = "classroom.centre.queue")
    public void consume(Message message, Message amqpMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        System.out.println("接收到消息：" + new String(message.getBody()));
        throw new RuntimeException("消费异常");
    }
}
