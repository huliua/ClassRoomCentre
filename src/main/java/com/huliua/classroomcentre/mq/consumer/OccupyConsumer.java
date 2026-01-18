package com.huliua.classroomcentre.mq.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.huliua.classroomcentre.service.ClassRoomService;
import com.huliua.common.domain.BusinessException;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OccupyConsumer {

    private final ClassRoomService classRoomService;

    @RabbitListener(queues = "classroom.centre.queue")
    public void consume(String messageBody, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("接收到消息：{}", messageBody);
        try {
            JSONObject messageObj = JSONUtil.parseObj(messageBody);
            String classroomId = messageObj.getStr("classroomId");
            String userId = messageObj.getStr("msgId");
            try {
                classRoomService.doOccupy(classroomId, userId);
            } catch (DuplicateKeyException de) {
                log.warn("幂等性拦截：消息已重复消费。classroomId={}, userId={}", classroomId, userId);
                channel.basicAck(deliveryTag, false);
                return;
            } catch (BusinessException ex) {
                if (ex.getCode() == -1) {
                    log.error("严重警告：Redis与DB库存不一致，DB扣减失败(教室已满)。id={}", classroomId);
                    channel.basicAck(deliveryTag, false);
                    return;
                }
                // 抛出异常，触发重试
                throw ex;
            }
            // 成功，确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息失败，重新入队", e);
            // 抛出异常，触发重试
            throw e;
        }
    }

    @RabbitListener(queues = "order.failed.dlq")
    public void consumeDeadQueue(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("接收到死信消息：{}", new String(message.getBody()));
            log.info("执行延时后的特殊逻辑...");
        } catch (Exception e) {
            log.error("处理死信消息时出错: ", e);
        }
    }
}