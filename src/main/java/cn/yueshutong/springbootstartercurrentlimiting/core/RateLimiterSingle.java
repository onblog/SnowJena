package cn.yueshutong.springbootstartercurrentlimiting.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶算法：单机、纳秒级流控
 */
public class RateLimiterSingle implements RateLimiter {
    private long size; //size：令牌桶容量
    private long period; //间隔：1000*1000/size 微秒
    private long initialDelay; //延迟生效时间：毫秒
    private AtomicLong bucket = new AtomicLong(0); //令牌桶初始容量：0

    /**
     * @param QPS 每秒并发量,等于0 默认禁止访问
     * @param initialDelay 首次延迟时间：毫秒
     */
    private RateLimiterSingle(double QPS, long initialDelay) {
        this.size = QPS < 1 ? 1 : Double.doubleToLongBits(QPS);
        this.initialDelay = initialDelay * 1000 * 1000; //毫秒转纳秒
        this.period = QPS != 0 ? Double.doubleToLongBits(1000 * 1000 * 1000 / QPS) : Integer.MAX_VALUE;
        if (QPS != 0) {
            putScheduled();
        }
    }

    public static RateLimiter of(double QPS, long initialDelay) {
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
    private void putScheduled() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> {
            if (size > bucket.longValue()) {
                bucket.incrementAndGet();
            }
        }, initialDelay, period, TimeUnit.NANOSECONDS);
    }

}
