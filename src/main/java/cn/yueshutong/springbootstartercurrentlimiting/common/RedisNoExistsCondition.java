package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 22:28
 */
@Configuration
public class RedisNoExistsCondition implements Condition {
    @Autowired(required = false)
    private RedisTemplate redisTemplate;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (redisTemplate==null){
            return true;
        }
        return false;
    }

}
