package cn.yueshutong.springbootstartercurrentlimiting.core;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.yueshutong.springbootstartercurrentlimiting.common.RedisLockUtil.*;

/**
 * 令牌桶算法：分布式、Redis
 */
public class RateLimiterCloud implements RateLimiter {
    private long QPS; //QPS：每秒并发数、令牌桶容量
    private long period; //间隔：1000*1000/QPS 微秒
    private long initialDelay; //初始延迟时间：毫秒
    private final int LOCK_GET_EXPIRES = 5*1000; //锁过期时间：毫秒
    private final int LOCK_PUT_EXPIRES = 10*1000; //实例过期时间：毫秒
    private String LOCK_GET; //（取出）互斥锁：ApplicationName(+MethodName)
    private String LOCK_PUT; //（存放）互斥锁：ApplicationName(+MethodName)
    private String BUCKET; //令牌桶
    private String LOCK_PUT_DATA; //记录上一次操作的时间
    private final String AppCode = SpringContextUtil.getApplicationName() + SpringContextUtil.getPort()+this.hashCode(); //唯一实例标识
    private StringRedisTemplate template = SpringContextUtil.getBean(StringRedisTemplate.class); //获取RedisTemplate

    private RateLimiterCloud(long QPS, long initialDelay, String bucket) {
        this.QPS = QPS;
        this.initialDelay = initialDelay * 1000;
        this.period = QPS > 0 ? 1000 * 1000 / QPS : Integer.MAX_VALUE; //提升至微秒
        this.LOCK_GET = bucket + "$GET";
        this.LOCK_PUT = bucket + "$PUT";
        this.BUCKET = bucket;
        this.LOCK_PUT_DATA = this.LOCK_PUT + "$DATA";
        initBucket();
        scheduled();
    }

    public static RateLimiter of(long QPS, long initialDelay, String bucket) {
        return new RateLimiterCloud(QPS, initialDelay, bucket);
    }

    /**
     * 获取令牌,阻塞直到成功
     */
    @Override
    public boolean tryAcquire() {
        try {
            tryLock(template, LOCK_GET, LOCK_GET, LOCK_GET_EXPIRES, TimeUnit.MILLISECONDS); //取到锁
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
        try {
            tryLock(template, LOCK_GET, LOCK_GET, LOCK_GET_EXPIRES, TimeUnit.MILLISECONDS); //取到锁
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
    private void scheduled() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (tryLockFailed(template, LOCK_PUT, AppCode) || AppCode.equals(template.opsForValue().get(LOCK_PUT))) { //成为leader
                    Long s = Long.valueOf(template.opsForValue().get(BUCKET));
                    if (QPS > s) {
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
        }, initialDelay, period, TimeUnit.MICROSECONDS);
    }

    /**
     * 初始化令牌桶
     */
    private void initBucket() {
        try {
            tryLock(template, LOCK_GET, LOCK_GET, LOCK_GET_EXPIRES, TimeUnit.MILLISECONDS); //取到锁
            if (!template.hasKey(BUCKET)) {
                template.opsForValue().set(BUCKET, String.valueOf(0));
            }
        } finally {
            releaseLock(template, LOCK_PUT); //释放锁
        }
    }

}
