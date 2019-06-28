package cn.yueshutong.core.limiter;

import java.util.concurrent.Executors;

public class RateLimiterDefaultTest {

//    @Test
    public void tryAcquire() throws InterruptedException {
        Executors.newScheduledThreadPool(2).execute(() -> {
            System.out.println("1");
        });
        Thread.sleep(1000);
    }
}