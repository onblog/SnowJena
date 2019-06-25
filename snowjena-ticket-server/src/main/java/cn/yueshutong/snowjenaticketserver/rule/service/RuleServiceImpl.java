package cn.yueshutong.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.redisson.SingleRedisLock;
import cn.yueshutong.snowjenaticketserver.rule.entity.Result;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RuleServiceImpl implements RuleService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SingleRedisLock redisLock;
    @Autowired
    private ScheduledExecutorService scheduledExecutor;

    private ValueOperations<String, String> valueOperations;

    @PostConstruct
    private void valueOperations() {
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public LimiterRule heartbeat(LimiterRule limiterRule) {
        //1.读取最新的规则
        LimiterRule newLimiterRule = readMostNewRule(limiterRule);
        //2.标记实例状况
        valueOperations.set(RuleService.getInstanceKey(newLimiterRule), RuleService.INSTANCE, 5, TimeUnit.SECONDS);
        //3.实时更新实例数量
        updateInstanceNumber(limiterRule);
        //4.检查令牌桶状况
        putTokenBucket(limiterRule);
        return newLimiterRule;
    }

    /**
     * 读取最新的规则
     */
    private LimiterRule readMostNewRule(LimiterRule limiterRule) {
        String rule = valueOperations.get(RuleService.getLimiterRuleKey(limiterRule));
        if (rule != null && !"".equals(rule)) {
            //更新
            LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
            if (limiter.getVersion() > limiterRule.getVersion()) {
                limiter.setName(limiterRule.getName());
                return limiter;
            }
        }else {
            //添加
            valueOperations.set(RuleService.getLimiterRuleKey(limiterRule),JSON.toJSONString(limiterRule));
        }
        return limiterRule;
    }

    /**
     * 更新实例数
     */
    private void updateInstanceNumber(LimiterRule limiterRule) {
        Set<String> keys = redisTemplate.keys(RuleService.getInstanceKeys(limiterRule));
        assert keys != null;
        int size = keys.size();
        limiterRule.setNumber(size);
    }

    /**
     * 检查令牌桶状况
     */
    private void putTokenBucket(LimiterRule limiterRule) {
        Boolean result = valueOperations.setIfAbsent(RuleService.getBucketKey(limiterRule), String.valueOf(limiterRule.getLimit()), limiterRule.getUnit().toSeconds(limiterRule.getInitialDelay())+1,TimeUnit.SECONDS);
        if (result == null || !result) {
            return; //该令牌桶已有线程负责存放
        }
        //更新令牌桶
        scheduledExecutor.scheduleAtFixedRate(() -> valueOperations.set(RuleService.getBucketKey(limiterRule), String.valueOf(limiterRule.getLimit()), limiterRule.getUnit().toSeconds(limiterRule.getPeriod())+1,TimeUnit.SECONDS), limiterRule.getInitialDelay(), limiterRule.getPeriod(), limiterRule.getUnit());
    }

    @Override
    public boolean update(LimiterRule limiterRule) {
        //加锁
        redisLock.acquire(RuleService.getLockKey(limiterRule));
        String key = RuleService.getLimiterRuleKey(limiterRule);
        String rule = valueOperations.get(key);
        //规则不存在
        if (rule == null || "".equals(rule)) {
            //解锁
            redisLock.release(LOCK + key);
            return false;
        }
        //更新版本号
        LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
        limiterRule.setVersion(limiter.getVersion() + 1);
        //更新规则
        valueOperations.set(key, JSON.toJSONString(limiterRule), 5, TimeUnit.SECONDS); //3
        //解锁
        redisLock.release(RuleService.getLockKey(limiterRule));
        return true;
    }

    public Result<LimiterRule> getAllRule(String app, String id, int page, int limit) {
        String builder = (app == null ? "" : app) +
                (id == null ? "" : id);
        Set<String> keys = redisTemplate.keys(RuleService.RULE + builder);
        assert keys != null;
        //result
        Result<LimiterRule> result = new Result<>();
        result.setCount(keys.size());
        //list
        List<LimiterRule> limiterRules = new ArrayList<>();
        keys.stream().skip((page - 1) * limit).limit(limit)
                .forEach(s -> {
                    String s1 = valueOperations.get(s);
                    LimiterRule limiterRule = JSON.parseObject(s1, LimiterRule.class);
                    limiterRules.add(limiterRule);
                });
        result.setData(limiterRules);
        return result;
    }

    private String toArray(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(strings.get(i));
        }
        return builder.toString();
    }
}
