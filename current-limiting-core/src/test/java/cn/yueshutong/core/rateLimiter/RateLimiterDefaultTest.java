package cn.yueshutong.core.rateLimiter;

import org.junit.Test;

import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class RateLimiterDefaultTest {

    @Test
    public void tryAcquire() throws InterruptedException {
        Executors.newScheduledThreadPool(2).execute(() -> {
            System.out.println("1");
        });
        Thread.sleep(1000);
    }
}