package cn.yueshutong.springbootstartercurrentlimiting.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶算法：单机
 */
public class RateLimiterSingle implements RateLimiter {
    private long QPS; //QPS：每秒并发数、令牌桶容量
    private long period; //间隔：1000*1000/QPS 微秒
    private long initialDelay; //初始延迟时间：微秒
    private AtomicLong bucket = new AtomicLong(0); //令牌桶初始容量：0

    private RateLimiterSingle(long QPS, long initialDelay) {
        this.QPS = QPS;
        this.initialDelay = initialDelay*1000; //提升至微秒
        this.period = QPS > 0 ? 1000*1000 / QPS : Integer.MAX_VALUE;
        scheduled();
    }

    public static RateLimiter of(long QPS, long initialDelay) {
        return new RateLimiterSingle(QPS, initialDelay);
    }

    /**
     * CAS获取令牌,阻塞直到成功
     */
    @Override
    public boolean tryAcquire() {
        long l = bucket.longValue();
        while (!(l > 0 && bucket.compareAndSet(l, l - 1))) {
            l = bucket.longValue();
        }
        return true;
    }

    /**
     * CAS获取令牌,没有令牌立即失败
     */
    @Override
    public boolean tryAcquireFailed() {
        long l = bucket.longValue();
        while (l > 0) {
            if (bucket.compareAndSet(l, l - 1)) {
                return true;
            }
            l = bucket.longValue();
        }
        return false;
    }

    /**
     * 周期性放令牌，控制访问速率
     */
    private void scheduled() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> {
            if (QPS > bucket.longValue()) {
                bucket.incrementAndGet();
            }
        }, initialDelay, period, TimeUnit.MICROSECONDS);
    }

}
