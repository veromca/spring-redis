package com.lsq.spring.redis.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestRedis {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("myRedisTemplate")
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ObjectMapper objectMapper;

    public void testRedis(){
        System.out.println("hello redis");
        RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
        // 原生操作
        conn.set("hello01_key".getBytes(),"lsq中国".getBytes());
        System.out.println(new String(conn.get("hello01_key".getBytes())));
        // 支持对象序列化
        stringRedisTemplate.opsForValue().set("str_k1","test str");
        System.out.println(stringRedisTemplate.opsForValue().get("str_k1"));
        // hash 对象
        HashOperations<String, Object, Object> hash = stringRedisTemplate.opsForHash();
        hash.put("sean","name","zhouzhilei");
        hash.put("sean","age","22");
        System.out.println(hash.entries("sean"));
        // Jackson2HashMapper
        Person person = new Person();
        person.setName("zhangsan");
        person.setAge(19);
        Jackson2HashMapper jm = new Jackson2HashMapper(objectMapper, false);
        stringRedisTemplate.opsForHash().putAll("sean01",jm.toHash(person));

        Map map = stringRedisTemplate.opsForHash().entries("sean01");

        Person per = objectMapper.convertValue(map, Person.class);
        System.out.println(per.getName());

        // 发布订阅
        stringRedisTemplate.convertAndSend("ooxx","hello");
        RedisConnection cc = stringRedisTemplate.getConnectionFactory().getConnection();
        cc.subscribe(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                byte[] body = message.getBody();
                System.out.println(new java.lang.String(body));
            }
        }, "ooxx".getBytes());

        while (true){
            stringRedisTemplate.convertAndSend("ooxx","hello from myself");
            try{
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
