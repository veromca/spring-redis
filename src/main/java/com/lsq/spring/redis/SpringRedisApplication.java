package com.lsq.spring.redis;

import com.lsq.spring.redis.redisson.TestRedLock;
import com.lsq.spring.redis.redisson.TestRedisson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringRedisApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SpringRedisApplication.class, args);
        // spring-redis
        //TestRedis redis = ctx.getBean(TestRedis.class);
        //redis.testRedis();

        // 普通分布式锁
//        TestRedisson redisson = ctx.getBean(TestRedisson.class);
//        redisson.testRedisson();
        // redlock
        TestRedLock redLock = ctx.getBean(TestRedLock.class);
        redLock.testRedLock();

    }

}
