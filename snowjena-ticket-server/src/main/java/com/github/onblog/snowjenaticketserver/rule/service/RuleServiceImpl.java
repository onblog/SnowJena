package com.github.onblog.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.RateLimiterRule;
import com.github.onblog.snowjenaticketserver.exception.ResultEnum;
import com.github.onblog.snowjenaticketserver.exception.TicketServerException;
import com.github.onblog.snowjenaticketserver.redisson.SingleRedisLock;
import com.github.onblog.snowjenaticketserver.rule.entity.Result;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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

    private Map<String, ScheduledFuture> taskMap = new ConcurrentHashMap<>();

    private String id = UUID.randomUUID().toString();

    private Logger logger = LoggerFactory.getLogger(RuleService.class);

    @PostConstruct
    private void valueOperations() {
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public RateLimiterRule heartbeat(RateLimiterRule rateLimiterRule) {
        //1.读取最新的规则
        RateLimiterRule nowRateLimiterRule = readMostNewRule(rateLimiterRule);
        //2.标记实例状况
        valueOperations.set(RuleService.getInstanceKey(nowRateLimiterRule), RuleService.INSTANCE, 5, TimeUnit.SECONDS);
        //3.实时更新实例数量
        updateInstanceNumber(nowRateLimiterRule);
        //4.检查令牌桶状况
        putTokenBucket(nowRateLimiterRule, rateLimiterRule);
        return nowRateLimiterRule;
    }

    /**
     * 读取最新的规则
     */
    private RateLimiterRule readMostNewRule(RateLimiterRule rateLimiterRule) {
        String rule = valueOperations.get(RuleService.getLimiterRuleKey(rateLimiterRule));
        if (rule == null || "".equals(rule)) {
            //规则添加
            valueOperations.set(RuleService.getLimiterRuleKey(rateLimiterRule), JSON.toJSONString(rateLimiterRule), 5, TimeUnit.SECONDS);
        } else {
            //规则延时
            redisTemplate.expire(RuleService.getLimiterRuleKey(rateLimiterRule), 5, TimeUnit.SECONDS);
            //读取最新
            RateLimiterRule limiter = JSON.parseObject(rule, RateLimiterRule.class);
            if (limiter.getVersion() > rateLimiterRule.getVersion()) {
                limiter.setName(rateLimiterRule.getName());
                return limiter;
            }
        }
        return rateLimiterRule;
    }

    /**
     * 更新实例数
     */
    private void updateInstanceNumber(RateLimiterRule rateLimiterRule) {
        Set<String> keys = redisTemplate.keys(RuleService.getInstanceKeys(rateLimiterRule));
        if (keys != null) {
            int size = keys.size();
            rateLimiterRule.setNumber(size);
        }
    }

    /**
     * 检查令牌桶状况
     */
    private void putTokenBucket(RateLimiterRule nowRateLimiterRule, RateLimiterRule oldRateLimiterRule) {
        //检查该令牌桶负责人
        Boolean result = valueOperations.setIfAbsent(RuleService.getBucketPrincipalKey(nowRateLimiterRule), id,
                nowRateLimiterRule.getUnit().toSeconds(nowRateLimiterRule.getInitialDelay()) + 1, TimeUnit.SECONDS);
        if (!result) {
            //该令牌桶已有线程负责存放
            String name = valueOperations.get(RuleService.getBucketPrincipalKey(nowRateLimiterRule));
            if (!id.equals(name)) {
                //而且还不是自己
                return;
            }
        }
        //分配向桶里存放令牌的任务
        if (taskMap.containsKey(RuleService.getBucketKey(nowRateLimiterRule))) {
            if (nowRateLimiterRule.getVersion() > oldRateLimiterRule.getVersion()) {
                ScheduledFuture scheduledFuture = taskMap.get(RuleService.getBucketKey(nowRateLimiterRule));
                scheduledFuture.cancel(true);
            } else {
                return;
            }
        }
        //执行任务
        ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            String rule = valueOperations.get(RuleService.getLimiterRuleKey(nowRateLimiterRule));
            if (rule == null || "".equals(rule)) {
                logger.debug("task cancel : " + RuleService.getLimiterRuleKey(nowRateLimiterRule));
                taskMap.get(RuleService.getBucketKey(nowRateLimiterRule)).cancel(true);
                taskMap.remove(RuleService.getBucketKey(nowRateLimiterRule));
                return;
            }
            valueOperations.set(RuleService.getBucketKey(nowRateLimiterRule), String.valueOf(nowRateLimiterRule.getLimit()),
                    nowRateLimiterRule.getUnit().toSeconds(nowRateLimiterRule.getPeriod()) + 1, TimeUnit.SECONDS);
        }, nowRateLimiterRule.getInitialDelay(), nowRateLimiterRule.getPeriod(), nowRateLimiterRule.getUnit());
        taskMap.put(RuleService.getBucketKey(nowRateLimiterRule), scheduledFuture);
    }

    @Override
    public boolean update(RateLimiterRule rateLimiterRule) {
        //加锁
        redisLock.acquire(RuleService.getLockKey(rateLimiterRule));
        String key = RuleService.getLimiterRuleKey(rateLimiterRule);
        String rule = valueOperations.get(key);
        //规则不存在
        if (rule == null || "".equals(rule)) {
            //解锁
            redisLock.release(LOCK + key);
            throw new TicketServerException(ResultEnum.ERROR, "规则不存在");
        }
        //更新版本号
        RateLimiterRule limiter = JSON.parseObject(rule, RateLimiterRule.class);
        rateLimiterRule.setVersion(limiter.getVersion() + 1);
        //更新规则
        valueOperations.set(key, JSON.toJSONString(rateLimiterRule), 5, TimeUnit.SECONDS); //3
        //解锁
        redisLock.release(RuleService.getLockKey(rateLimiterRule));
        return true;
    }

    @Override
    public Result<RateLimiterRule> getAllRule(String app, String id, int page, int limit) {
        Set<String> keys = redisTemplate.keys(RuleService.getLimiterRuleKeys(app, id));
        if (keys == null) {
            throw new TicketServerException(ResultEnum.ERROR, RuleService.getLimiterRuleKeys(app, id) + " return null");
        }
        //result
        Result<RateLimiterRule> result = new Result<>(ResultEnum.SUCCESS);
        result.setCount(keys.size());
        //list
        List<RateLimiterRule> rateLimiterRules = new ArrayList<>();
        keys.stream().skip((page - 1) * limit).limit(limit)
                .forEach(s -> {
                    String s1 = valueOperations.get(s);
                    RateLimiterRule rateLimiterRule = JSON.parseObject(s1, RateLimiterRule.class);
                    updateInstanceNumber(rateLimiterRule);
                    rateLimiterRules.add(rateLimiterRule);
                });
        result.setData(rateLimiterRules);
        return result;
    }
}
