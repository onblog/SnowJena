package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisLockUtil {
    /**
     * 获取锁,获取不到立即失败
     */
    public static boolean tryLockFailed(StringRedisTemplate template,String Key,String Value) {
        Boolean b = template.opsForValue().setIfAbsent(Key, Value);
        return b == null ? false : b;
    }

    /**
     * 释放锁
     */
    public static void releaseLock(StringRedisTemplate template,String LOCK) {
        template.delete(LOCK);
    }

}