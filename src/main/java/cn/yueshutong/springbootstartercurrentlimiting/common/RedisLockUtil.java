package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisLockUtil {
    /**
     * Failed to acquire lock immediately
     */
    public static boolean tryLockFailed(StringRedisTemplate template,String Key,String Value) {
        Boolean b = template.opsForValue().setIfAbsent(Key, Value);
        return b == null ? false : b;
    }

    /**
     * Release the lock
     */
    public static void releaseLock(StringRedisTemplate template,String LOCK) {
        template.delete(LOCK);
    }

}