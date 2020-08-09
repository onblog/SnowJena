package com.github.onblog.snowjenaticketserver.redisson;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SingleRedisLock {
    @Autowired
    private RedissonClient redisson;

    private final String LOCK_TITLE = "redisLock_";

    public void acquire(String lockName){
        String key = LOCK_TITLE + lockName;
        RLock mylock = redisson.getLock(key);
        mylock.lock(5, TimeUnit.MINUTES); //lock提供带timeout参数，timeout结束强制解锁，防止死锁
    }

    public void release(String lockName){
        String key = LOCK_TITLE + lockName;
        RLock mylock = redisson.getLock(key);
        mylock.unlock();
    }
}