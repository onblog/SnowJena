package cn.yueshutong.snowjenaticketserver.token.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.redisson.SingleRedisLock;
import cn.yueshutong.snowjenaticketserver.rule.service.RuleService;
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
    public Long token(LimiterRule limiterRule) {
        String s = valueOperations.get(RuleService.getBucketKey(limiterRule));
        if (s==null||"".equals(s)){
            return 0L;
        }
        long l = Long.parseLong(s);
        if (l>0){
            //加锁
            redisLock.acquire(RuleService.getLockKey(limiterRule));
            l = Long.parseLong(valueOperations.get(RuleService.getBucketKey(limiterRule)));
            long result;
            if (l<=0){
                result =  0L;
            }else if (l>=limiterRule.getLimit()){
                valueOperations.decrement(RuleService.getBucketKey(limiterRule),limiterRule.getLimit());
                result = limiterRule.getLimit();
            }else {
                valueOperations.decrement(RuleService.getBucketKey(limiterRule),l);
                result = l;
            }
            //解锁
            redisLock.release(RuleService.getLockKey(limiterRule));
            return result;
        }
        return 0L;
    }
}
