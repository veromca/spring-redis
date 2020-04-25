package com.lsq.spring.redis.redisson;

import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestRedisson {
    private static String lockKey = "lsq_lock";
    public void testRedisson(){
        System.out.println("hello redisson!");
        // 单机模式
        //testSingleLock();
        testSingleLockWithLeaseTime();
        // 哨兵模式
        //testSentinelLock();
        //集群模式
        testClusterLock();


    }

    /**
     * 单机模式：
     * 多线程模拟并发调用场景，对某段代码进行加锁
     * 通过 redis-cli:  hgetall lsq_lock
     * 1) "c1d96929-4cac-4053-ba9b-871adbd7ce0b:1"
     * 2) "1"
     * 得出，Redisson锁是一个hash结构，key为lsq_lock
     * 属性field是 UUID+threadId 的唯一ID
     * value就是重入值，在分布式锁时，这个值为1（Redisson还可以实现重入锁，那么这个值就取决于重入次数了）
     * 问题：锁不带过期时间，如果某个线程一直不释放锁资源，将造成死锁堵塞现象
     */
    private void testSingleLock(){
        List<Thread> lists = new ArrayList<Thread>();
        for(int i=0;i<10;i++){
            Thread t = new Thread(() ->{
                RLock rLock = RedissLockUtil.lock(lockKey);
                //RLock rLock = RedissLockUtil.tryLock(lockKey,1,10);
                try {
                    if(rLock.isLocked()){
                        System.out.println(Thread.currentThread().getName()+ " get "+lockKey);
                        // do something
                        Thread.sleep(30000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    rLock.unlock();
                    System.out.println(Thread.currentThread().getName()+  " unlock "+lockKey);
                }
            },"Thread-"+i);
            lists.add(t);
        }
        lists.forEach((a)->a.start());
    }

    /**
     * 带过期时间的锁：
     * 设置失效时间10秒 如果由于某些原因导致10秒还没执行完任务，这时候锁自动失效，导致其他线程也会拿到分布式锁。
     */
    private void testSingleLockWithLeaseTime(){
        List<Thread> lists = new ArrayList<Thread>();
        for(int i=0;i<10;i++){
            Thread t = new Thread(() ->{

                RLock rLock = RedissLockUtil.tryLock(lockKey,1,10);
                try {
                    if(rLock.isLocked()){
                        System.out.println(Thread.currentThread().getName()+ " get "+lockKey);
                        // do something
                        Thread.sleep(20000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    rLock.unlock();
                    System.out.println(Thread.currentThread().getName()+  " unlock "+lockKey);
                }
            },"Thread-"+i);
            lists.add(t);
        }
        lists.forEach((a)->a.start());
    }

    public void testSentinelLock(){
        List<Thread> lists = new ArrayList<Thread>();
        for(int i=0;i<10;i++){
            Thread t = new Thread(() ->{
                RLock rLock = RedissLockUtil.tryLock(lockKey,10,60);
                try {
                    if(rLock.isLocked()){
                        System.out.println(Thread.currentThread().getName()+ " get "+lockKey);
                        // do something
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    rLock.unlock();
                    System.out.println(Thread.currentThread().getName()+  " unlock "+lockKey);
                }
            },"Thread-"+i);
            lists.add(t);
        }
        lists.forEach((a)->a.start());

    }

    public void testClusterLock(){
        List<Thread> lists = new ArrayList<Thread>();
        for(int i=0;i<10;i++){
            Thread t = new Thread(() ->{
                RLock rLock = RedissLockUtil.tryLock(lockKey,1,60);
                try {
                    if(rLock.isLocked()){
                        System.out.println(Thread.currentThread().getName()+ " get "+lockKey);
                        // do something
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    rLock.unlock();
                    System.out.println(Thread.currentThread().getName()+  " unlock "+lockKey);
                }
            },"Thread-"+i);
            lists.add(t);
        }
        lists.forEach((a)->a.start());
    }


}
