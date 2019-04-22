package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisLockUtil {
    /**
     * 获取锁,阻塞直到成功
     */
    public static boolean tryLock(StringRedisTemplate template, String LOCK, String Value, int expires, TimeUnit unit) {
        Boolean b = template.opsForValue().setIfAbsent(LOCK, Value, expires, unit);
        while (!(b == null ? false : b)) {
            b = template.opsForValue().setIfAbsent(LOCK, Value, expires, unit);
        }
        return true;
    }

    /**
     * 获取锁,获取不到立即失败
     */
    public static boolean tryLockFailed(StringRedisTemplate template,String LOCK,String Value,int expires,TimeUnit unit) {
        Boolean b = template.opsForValue().setIfAbsent(LOCK, Value, expires, unit);
        return b == null ? false : b;
    }
    /**
     * 获取锁,获取不到立即失败
     */
    public static boolean tryLockFailed(StringRedisTemplate template,String LOCK,String Value) {
        Boolean b = template.opsForValue().setIfAbsent(LOCK, Value);
        return b == null ? false : b;
    }

    /**
     * 释放锁
     */
    public static boolean releaseLock(StringRedisTemplate template,String LOCK) {
        return template.delete(LOCK);
    }
}
