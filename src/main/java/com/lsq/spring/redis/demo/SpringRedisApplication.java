package com.lsq.spring.redis.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringRedisApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SpringRedisApplication.class, args);
        TestRedis redis = ctx.getBean(TestRedis.class);
        redis.testRedis();
    }

}
