package com.github.onblog.snowjenaticketserver.token.service;

import cn.yueshutong.commoon.entity.RateLimiterRule;
import com.github.onblog.snowjenaticketserver.redisson.SingleRedisLock;
import com.github.onblog.snowjenaticketserver.rule.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SingleRedisLock redisLock;

    private ValueOperations<String, String> valueOperations;

    @PostConstruct
    private void valueOperations() {
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public Long token(RateLimiterRule rateLimiterRule) {
        String s = valueOperations.get(RuleService.getBucketKey(rateLimiterRule));
        if (s==null||"".equals(s)){
            return 0L;
        }
        long l = Long.parseLong(s);
        if (l>0){
            //加锁
            redisLock.acquire(RuleService.getLockKey(rateLimiterRule));
            l = Long.parseLong(valueOperations.get(RuleService.getBucketKey(rateLimiterRule)));
            long result;
            if (l<=0){
                result =  0L;
            }else if (l>= rateLimiterRule.getLimit()){
                valueOperations.decrement(RuleService.getBucketKey(rateLimiterRule), rateLimiterRule.getLimit());
                result = rateLimiterRule.getLimit();
            }else {
                valueOperations.decrement(RuleService.getBucketKey(rateLimiterRule),l);
                result = l;
            }
            //解锁
            redisLock.release(RuleService.getLockKey(rateLimiterRule));
            return result;
        }
        return 0L;
    }
}
