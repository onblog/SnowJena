package cn.yueshutong.springbootstartercurrentlimiting.core;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.yueshutong.springbootstartercurrentlimiting.common.RedisLockUtil.*;

/**
 * 令牌桶算法：分布式、Redis
 */
public class RateLimiterCloud implements RateLimiter {
    private long size; //令牌桶容量
    private long period; //间隔时间：纳秒
    private long initialDelay; //延迟生效时间：毫秒
    private final int LOCK_GET_EXPIRES = 10 * 1000; //锁过期时间：毫秒
    private final int LOCK_PUT_EXPIRES = 10 * 1000; //实例过期时间：毫秒
    private String LOCK_GET; // 读锁
    private String LOCK_PUT; // 写锁
    private String BUCKET; //令牌桶标识
    private String LOCK_PUT_DATA; //记录上一次操作的时间
    private LocalDateTime ExpirationTime; //限流器对象到期时间
    private final String AppCode = SpringContextUtil.getApplicationName() + SpringContextUtil.getPort() + this.hashCode(); //唯一实例标识
    private StringRedisTemplate template = SpringContextUtil.getBean(StringRedisTemplate.class); //获取RedisTemplate

    /**
     * @param QPS          每秒并发量,等于0 默认禁止访问
     * @param initialDelay 首次延迟时间：毫秒
     * @param overflow     是否严格控制请求速率和次数
     */
    private RateLimiterCloud(double QPS, long initialDelay, String bucket, boolean overflow) {
        this.size = overflow ? 1 : (QPS < 1 ? 1 : new Double(QPS).longValue());
        this.initialDelay = initialDelay * 1000 * 1000; //毫秒转纳秒
        this.period = QPS != 0 ? new Double(1000 * 1000 * 1000 / QPS).longValue() : Integer.MAX_VALUE;
        init(bucket);
        if (QPS != 0) { //等于0就不放令牌了
            putScheduled();
        }
    }

    private void init(String bucket) {
        this.BUCKET = bucket;
        this.LOCK_GET = bucket + "$GET";
        this.LOCK_PUT = bucket + "$PUT";
        this.LOCK_PUT_DATA = this.LOCK_PUT + "$DATA";
        template.opsForValue().set(BUCKET, String.valueOf(0)); //初始化令牌桶为0
    }


    public static RateLimiter of(double QPS, long initialDelay, String bucket, boolean overflow) {
        return new RateLimiterCloud(QPS, initialDelay, bucket, overflow);
    }

    public static RateLimiter of(double QPS, long initialDelay, String bucket, boolean overflow, long time, ChronoUnit unit) {
        RateLimiterCloud rateLimiterCloud = new RateLimiterCloud(QPS, initialDelay, bucket, overflow);
        LocalDateTime localDateTime = LocalDateTime.now().plus(time,unit);
        rateLimiterCloud.setExpirationTime(localDateTime);
        return rateLimiterCloud;
    }

    /**
     * 获取令牌,阻塞直到成功
     */
    @Override
    public boolean tryAcquire() {
        tryLock(template, LOCK_GET, LOCK_GET, LOCK_GET_EXPIRES, TimeUnit.MILLISECONDS); //取到锁
        try {
            Long s = Long.valueOf(template.opsForValue().get(BUCKET));
            while (s <= 0) { //阻塞
                s = Long.valueOf(template.opsForValue().get(BUCKET));
            }
            template.opsForValue().decrement(BUCKET); //拿走令牌
            return true;
        } finally {
            releaseLock(template, LOCK_GET); //释放锁
        }
    }

    /**
     * 获取令牌,没有令牌立即失败
     */
    @Override
    public boolean tryAcquireFailed() {
        tryLock(template, LOCK_GET, LOCK_GET, LOCK_GET_EXPIRES, TimeUnit.MILLISECONDS); //取到锁
        try {
            Long s = Long.valueOf(template.opsForValue().get(BUCKET));
            if (s > 0) {
                template.opsForValue().decrement(BUCKET); //拿走令牌
                return true;
            }
            return false;
        } finally {
            releaseLock(template, LOCK_GET); //释放锁
        }
    }

    /**
     * 周期性放令牌，控制访问速率
     * 算法：通过抢占机制选举leader，其它候选者对leader进行监督，发现leader懈怠即可将其踢下台。由此进入新一轮的抢占...
     */
    private void putScheduled() {
        RateLimiter.scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (tryLockFailed(template, LOCK_PUT, AppCode) || AppCode.equals(template.opsForValue().get(LOCK_PUT))) { //成为leader
                    Long s = Long.valueOf(template.opsForValue().get(BUCKET));
                    if (size > s) {
                        template.opsForValue().increment(BUCKET);
                    }
                    template.opsForValue().set(LOCK_PUT_DATA, String.valueOf(System.currentTimeMillis()));//更新时间
                } else { //成为候选者
                    Long s = Long.valueOf(template.opsForValue().get(LOCK_PUT_DATA));
                    if (System.currentTimeMillis() - s > LOCK_PUT_EXPIRES) {
                        releaseLock(template, LOCK_PUT); //释放锁
                    }
                }
            }
        }, initialDelay, period, TimeUnit.NANOSECONDS);
    }

    @Override
    public LocalDateTime getExpirationTime() {
        return ExpirationTime;
    }

    private void setExpirationTime(LocalDateTime expirationTime) {
        ExpirationTime = expirationTime;
    }

}
