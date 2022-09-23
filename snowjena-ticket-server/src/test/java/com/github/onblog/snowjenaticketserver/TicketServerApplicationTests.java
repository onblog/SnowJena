package com.github.onblog.snowjenaticketserver;

import com.github.onblog.snowjenaticketserver.redisson.SingleRedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TicketServerApplicationTests {

    @Autowired
    private SingleRedisLock lock;

    @Autowired
    private RedisProperties redisProperties;

    @Test
    public void contextLoads() {
        System.out.println(redisProperties.getHost() + redisProperties.getPort() + redisProperties.getPassword());
        Executors.newScheduledThreadPool(100).scheduleAtFixedRate(() -> {
            lock.acquire("key");
            System.err.println("======lock  ======" + Thread.currentThread().getName());
            try {
                System.out.println("===========");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.release("key");
            System.err.println("======unlock======" + Thread.currentThread().getName());
        }, 0, 1, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
