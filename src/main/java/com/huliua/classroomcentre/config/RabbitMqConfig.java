package com.huliua.classroomcentre.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMqConfig {

    @Lazy
    @Resource
    private Queue myQueue;

    @Lazy
    @Resource
    private Queue myDelayQueue;

    @Lazy
    @Resource
    private DirectExchange myExchange;

    @Lazy
    @Resource
    private Queue dlq;

    @Lazy
    @Resource
    private DirectExchange dlxExchange;

    @Bean
    public DirectExchange myExchange() {
        return new DirectExchange("classroom-centre-exchange", true, false);
    }

    @Bean
    public Queue myQueue() {
        return QueueBuilder.durable("classroom.centre.queue")
                .deadLetterExchange("dlx.exchange")
                .deadLetterRoutingKey("dlx.order.failed")
                .build();
    }

    @Bean
    public Queue myDelayQueue() {
        return QueueBuilder.durable("delay.queue")
                .deadLetterExchange("dlx.exchange")
                .deadLetterRoutingKey("dlx.order.failed")
                .build();
    }

    @Bean
    public Binding bindingMyQueueToExchange() {
        return BindingBuilder.bind(myQueue).to(myExchange).with("classroom.occupy");
    }

    @Bean
    public Binding bindingDelayQueueToExchange() {
        return BindingBuilder.bind(myDelayQueue).to(myExchange).with("delay");
    }

    @Bean
    public Queue dlq() {
        return new Queue("order.failed.dlq");
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlx.exchange");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("dlx.order.failed");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // 设置消息转换器（推荐使用 JSON）
        template.setMessageConverter(new Jackson2JsonMessageConverter());

        // 设置 ConfirmCallback (核心：发送确认)
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData 相关数据（可用于关联消息ID）
             * @param ack true=ACK, false=NACK
             * @param cause 失败原因（NACK时）
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    log.info("✅ 消息已确认 (ACK) - CorrelationData: {}", correlationData);
                } else {
                    log.info("❌ 消息被拒绝 (NACK) - CorrelationData: {}, 原因: {}", correlationData, cause);
                    // 放入死信队列中
                    template.send("dlx.exchange", "dlx.order.failed", new Message("消息被拒绝".getBytes(), new MessageProperties()));
                }
            }
        });

        return template;
    }
}