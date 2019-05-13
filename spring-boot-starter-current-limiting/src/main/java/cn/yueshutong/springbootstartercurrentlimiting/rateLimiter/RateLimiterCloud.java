package cn.yueshutong.springbootstartercurrentlimiting.rateLimiter;

import cn.yueshutong.springbootstartercurrentlimiting.common.RedisLockUtil;
import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.common.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 令牌桶算法：分布式、Redis、无锁
 */
public class RateLimiterCloud implements RateLimiter {
    private long size; //令牌桶容量
    private long period; //间隔时间：纳秒
    private long initialDelay; //延迟生效时间：毫秒
    private final int BUCKET_PUT_EXPIRES = 10 * 1000; //实例过期时间：毫秒
    private String BUCKET_PUT; // 放令牌的标识
    private String BUCKET; //令牌桶标识
    private String BUCKET_PUT_DATE; //记录上一次操作的时间
    private LocalDateTime expirationTime; //限流器对象到期时间
    private final String AppCode = SpringContextUtil.getApplicationName() + SpringContextUtil.getPort() + this.hashCode(); //唯一实例标识
    private StringRedisTemplate template = SpringContextUtil.getBean(StringRedisTemplate.class); //获取RedisTemplate
    private DefaultRedisScript redisScript = SpringContextUtil.getBean(DefaultRedisScript.class); //Redis-Lua
    private List<String> keys = new ArrayList<>(4); //Lua-Keys
    private Logger logger = LoggerFactory.getLogger(RateLimiterCloud.class);
    private RateLimiterSingle rateLimiterSingle = null; //Redis挂掉之后替换为单机限流

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
        this.BUCKET_PUT = bucket + "$PUT";
        this.BUCKET_PUT_DATE = this.BUCKET_PUT + "$DATA";
        template.opsForValue().set(BUCKET, String.valueOf(0)); //初始化令牌桶为0
    }


    public static RateLimiter of(double QPS, long initialDelay, String bucket, boolean overflow) {
        return new RateLimiterCloud(QPS, initialDelay, bucket, overflow);
    }

    public static RateLimiter of(double QPS, long initialDelay, String bucket, boolean overflow, long time, ChronoUnit unit) {
        RateLimiterCloud rateLimiterCloud = new RateLimiterCloud(QPS, initialDelay, bucket, overflow);
        if (unit != null) {
            LocalDateTime localDateTime = LocalDateTime.now().plus(time, unit);
            rateLimiterCloud.setExpirationTime(localDateTime);
        }
        return rateLimiterCloud;
    }

    /**
     * 获取令牌,阻塞直到成功
     */
    @Override
    public boolean tryAcquire() {
        if (this.period == Integer.MAX_VALUE) { //QPS为0
            return false;
        }
        try {
            Long d = template.opsForValue().increment(BUCKET, -1);
            while (d < 0) { //无效令牌
                sleep();
                d = template.opsForValue().increment(BUCKET, -1);
            }
        } catch (Exception e) { //熔断降级
            logger.error("Redis is unavailable! Has been switched to single-machine current limiting.");
            if (rateLimiterSingle==null){
                rateLimiterSingle = new RateLimiterSingle(size,period,initialDelay,expirationTime);
            }
            return rateLimiterSingle.tryAcquire();
        }
        return true;
    }

    private void sleep() {
        if (this.period < 1000 * 1000) {
            return;
        }
        try {
            Thread.sleep(new Double(this.period / 1000 / 1000).longValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取令牌,没有令牌立即失败
     */
    @Override
    public boolean tryAcquireFailed() {
        try {
            Long d = template.opsForValue().increment(BUCKET, -1);
            if (d < 0) { //无效令牌
                return false;
            }
        } catch (Exception e) { //熔断降级
            logger.error("Redis is unavailable! Has been switched to single-machine current limiting.");
            if (rateLimiterSingle==null){
                rateLimiterSingle = new RateLimiterSingle(size,period,initialDelay,expirationTime);
            }
            return rateLimiterSingle.tryAcquireFailed();
        }
        return true;
    }

    /**
     * 周期性放令牌，控制访问速率
     * 选举算法：通过抢占机制选举leader，其它候选者对leader进行监督，发现leader懈怠即可将其踢下台。由此进入新一轮的抢占...
     */
    private void putScheduled() {
        ThreadPool.scheduled.scheduleAtFixedRate(() -> {
            String appCode = template.opsForValue().get(BUCKET_PUT);
            if (AppCode.equals(appCode) || (appCode == null && RedisLockUtil.tryLockFailed(template, BUCKET_PUT, AppCode))) { //成为leader
                putBucket();
            } else { //成为候选者
                Long s = Long.valueOf(Objects.requireNonNull(template.opsForValue().get(BUCKET_PUT_DATE)));
                if (System.currentTimeMillis() - s > BUCKET_PUT_EXPIRES) {
                    RedisLockUtil.releaseLock(template, BUCKET_PUT); //重新选举
                }else {
                    try {
                        Thread.sleep(BUCKET_PUT_EXPIRES); //休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, initialDelay, period, TimeUnit.NANOSECONDS);
    }

    /**
     * 调用Redis-Lua脚本、考虑对象复用
     */
    private void putBucket() {
        keys.add(BUCKET);
        keys.add(String.valueOf(size));
        keys.add(BUCKET_PUT_DATE);
        keys.add(String.valueOf(System.currentTimeMillis()));
        template.execute(redisScript, keys);//执行Lua脚本
        keys.clear();
    }

    @Override
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    private void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

}
