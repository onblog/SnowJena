package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 18:41
 */
@Configuration
public class RedisExistsCondition implements Condition {
    @Autowired(required = false)
    private RedisTemplate redisTemplate;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (redisTemplate==null){
            return false;
        }
        return true;
    }
}
