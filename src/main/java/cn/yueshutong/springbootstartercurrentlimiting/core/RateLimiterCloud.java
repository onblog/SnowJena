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
    private final int expires = 50; //过期时间：毫秒
    private String LOCK_GET; //（取出）互斥锁：ApplicationName(+MethodName)
    private String LOCK_PUT; //（存放）互斥锁：ApplicationName(+MethodName)
    private String BUCKET; //令牌桶
    private StringRedisTemplate template = SpringContextUtil.getBean(StringRedisTemplate.class); //获取RedisTemplate

    private RateLimiterCloud(long QPS, long initialDelay, String bucket) {
        this.QPS = QPS;
        this.initialDelay = initialDelay * 1000;
        this.period = QPS > 0 ? 1000 * 1000 / QPS : Integer.MAX_VALUE; //提升至微秒
        this.LOCK_GET = bucket+"$GET";
        this.LOCK_PUT = bucket+"$PUT";
        this.BUCKET = bucket;
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
            tryLock(template, LOCK_GET, LOCK_GET, expires, TimeUnit.MILLISECONDS); //取到锁
            Long s = Long.valueOf(template.opsForValue().get(BUCKET));
            while (s<=0){ //阻塞
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
            tryLock(template, LOCK_GET, LOCK_GET, expires, TimeUnit.MILLISECONDS); //取到锁
            Long s = Long.valueOf(template.opsForValue().get(BUCKET));
            if (s > 0){
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
     * Redis存储上一次放入令牌的时间
     */
    private void scheduled() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    tryLock(template, LOCK_PUT, LOCK_PUT, expires, TimeUnit.MILLISECONDS); //取到锁
                    Long s = Long.valueOf(template.opsForValue().get(BUCKET));
                    if (QPS > s){
                        template.opsForValue().increment(BUCKET);
                    }
                } finally {
                    releaseLock(template, LOCK_PUT); //释放锁
                }
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }

}
