package com.huliua.classroomcentre.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestConsumer {

    @RabbitListener(queues = "classroom.centre.queue")
    public void consume(Message message, Message amqpMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        System.out.println("接收到消息：" + new String(message.getBody()));
    }

    @RabbitListener(queues = "order.failed.dlq")
    public void consumeDeadQueue(Message message, Message amqpMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("接收到死信消息：{}", new String(message.getBody()));
            log.info("执行延时后的特殊逻辑...");
        } catch (Exception e) {
            log.error("处理死信消息时出错: ", e);
        }
    }
}