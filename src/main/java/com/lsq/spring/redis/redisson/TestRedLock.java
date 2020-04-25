package com.lsq.spring.redis.redisson;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RedLock是基于redis实现的分布式锁，它能够保证以下特性：
 * <p>
 * 互斥性：在任何时候，只能有一个客户端能够持有锁；避免死锁：
 * 当客户端拿到锁后，即使发生了网络分区或者客户端宕机，也不会发生死锁；（利用key的存活时间）
 * 容错性：只要多数节点的redis实例正常运行，就能够对外提供服务，加锁或者释放锁；
 * RedLock算法思想，意思是不能只在一个redis实例上创建锁，应该是在多个redis实例上创建锁，n / 2 + 1，必须在大多数redis节点上都成功创建锁，才能算这个整体的RedLock加锁成功，避免说仅仅在一个redis实例上加锁而带来的问题。
 * <p>
 * Redisson实现原理
 * Redisson中有一个MultiLock的概念，可以将多个锁合并为一个大锁，对一个大锁进行统一的申请加锁以及释放锁
 */
@Component
public class TestRedLock {

    public void testRedLock() {
        Config config1 = new Config();
        config1.useSingleServer().setAddress("redis://192.168.101.100:6379")
                .setDatabase(0);
        RedissonClient redissonClient1 = Redisson.create(config1);

        Config config2 = new Config();
        config2.useSingleServer().setAddress("redis://192.168.101.101:6379")
                .setDatabase(0);
        RedissonClient redissonClient2 = Redisson.create(config2);

        Config config3 = new Config();
        config3.useSingleServer().setAddress("redis://192.168.101.102:6379")
                .setDatabase(0);
        RedissonClient redissonClient3 = Redisson.create(config3);

        String resourceName = "REDLOCK";
        RLock lock1 = redissonClient1.getLock(resourceName);
        RLock lock2 = redissonClient2.getLock(resourceName);
        RLock lock3 = redissonClient3.getLock(resourceName);

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);

        List<Thread> lists = new ArrayList<Thread>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                boolean isLock;
                try {
                    isLock = redLock.tryLock(2, 1, TimeUnit.SECONDS);
                    if (isLock) {
                        System.out.println(Thread.currentThread().getName()+ " isLock = " + isLock);
                        //TODO if get lock success, do something;
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 无论如何, 最后都要解锁
                    System.out.println(Thread.currentThread().getName()+ " unlock");
                    redLock.unlock();
                }
            }, "Thread-" + i);
            lists.add(t);
        }
        lists.forEach((a) -> a.start());


    }

}
