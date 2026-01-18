package com.huliua.classroomcentre.config;

import com.huliua.classroomcentre.redis.consumer.RedisMsgConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisConfig {

    public static final String CHANNEL_TOPIC = "chat";

    @Bean("occupyScript")
    public DefaultRedisScript<Long> occupyScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 指向资源目录下的文件
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/occupy.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置 Key 和 Value 的序列化器为 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

    /**
     * 将消息监听器绑定到频道
     *
     * @param connectionFactory Redis连接工厂
     * @param redisMsgListener  消息监听器
     * @param channelTopic      频道
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, RedisMsgConsumer redisMsgListener, ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 将监听器绑定到频道
        container.addMessageListener(redisMsgListener, channelTopic);
        return container;
    }

    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic(CHANNEL_TOPIC);
    }
}