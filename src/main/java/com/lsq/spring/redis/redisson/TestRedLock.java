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
 *
 *为了取到锁，客户端应该执行以下操作:
 *
 * 获取当前Unix时间，以毫秒为单位。
 * 依次尝试从5个实例，使用相同的key和具有唯一性的value（例如UUID）获取锁。当向Redis请求获取锁时，客户端应该设置一个网络连接和响应超时时间，这个超时时间应该小于锁的失效时间。例如你的锁自动失效时间为10秒，则超时时间应该在5-50毫秒之间。这样可以避免服务器端Redis已经挂掉的情况下，客户端还在死死地等待响应结果。如果服务器端没有在规定时间内响应，客户端应该尽快尝试去另外一个Redis实例请求获取锁。
 * 客户端使用当前时间减去开始获取锁时间（步骤1记录的时间）就得到获取锁使用的时间。当且仅当从大多数（N/2+1，这里是3个节点）的Redis节点都取到锁，并且使用的时间小于锁失效时间时，锁才算获取成功。
 * 如果取到了锁，key的真正有效时间等于有效时间减去获取锁所使用的时间（步骤3计算的结果）。
 * 如果因为某些原因，获取锁失败（没有在至少N/2+1个Redis实例取到锁或者取锁时间已经超过了有效时间），客户端应该在所有的Redis实例上进行解锁（即便某些Redis实例根本就没有加锁成功，防止某些节点获取到锁但是客户端没有得到响应而导致接下来的一段时间不能被重新获取锁）。
 *

 *
 *
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
       // 向3个redis实例尝试加锁
        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        List<Thread> lists = new ArrayList<Thread>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                boolean isLock;
                try {
                    // 10s拿不到锁, 就认为获取锁失败。30s即30s是锁失效时间。
                    isLock = redLock.tryLock(10, 30, TimeUnit.SECONDS);
                    if (isLock) {
                        System.out.println(Thread.currentThread().getName()+ " isLock = " + isLock);
                        //TODO if get lock success, do something;
                        Thread.sleep(200);
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
