package com.lsq.spring.redis.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class MyTemplate {
    @Bean
    public StringRedisTemplate myRedisTemplate(RedisConnectionFactory fc) {
        StringRedisTemplate stp = new StringRedisTemplate(fc);
        stp.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return stp;
    }
}
