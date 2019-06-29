package cn.yueshutong.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.exception.ResultEnum;
import cn.yueshutong.snowjenaticketserver.exception.TicketServerException;
import cn.yueshutong.snowjenaticketserver.redisson.SingleRedisLock;
import cn.yueshutong.snowjenaticketserver.rule.entity.Result;
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
    public LimiterRule heartbeat(LimiterRule limiterRule) {
        //1.读取最新的规则
        LimiterRule nowLimiterRule = readMostNewRule(limiterRule);
        //2.标记实例状况
        valueOperations.set(RuleService.getInstanceKey(nowLimiterRule), RuleService.INSTANCE, 5, TimeUnit.SECONDS);
        //3.实时更新实例数量
        updateInstanceNumber(nowLimiterRule);
        //4.检查令牌桶状况
        putTokenBucket(nowLimiterRule, limiterRule);
        return nowLimiterRule;
    }

    /**
     * 读取最新的规则
     */
    private LimiterRule readMostNewRule(LimiterRule limiterRule) {
        String rule = valueOperations.get(RuleService.getLimiterRuleKey(limiterRule));
        if (rule == null || "".equals(rule)) {
            //规则添加
            valueOperations.set(RuleService.getLimiterRuleKey(limiterRule), JSON.toJSONString(limiterRule), 5, TimeUnit.SECONDS);
        } else {
            //规则延时
            redisTemplate.expire(RuleService.getLimiterRuleKey(limiterRule), 5, TimeUnit.SECONDS);
            //读取最新
            LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
            if (limiter.getVersion() > limiterRule.getVersion()) {
                limiter.setName(limiterRule.getName());
                return limiter;
            }
        }
        return limiterRule;
    }

    /**
     * 更新实例数
     */
    private void updateInstanceNumber(LimiterRule limiterRule) {
        Set<String> keys = redisTemplate.keys(RuleService.getInstanceKeys(limiterRule));
        if (keys != null) {
            int size = keys.size();
            limiterRule.setNumber(size);
        }
    }

    /**
     * 检查令牌桶状况
     */
    private void putTokenBucket(LimiterRule nowLimiterRule, LimiterRule oldLimiterRule) {
        //检查该令牌桶负责人
        Boolean result = valueOperations.setIfAbsent(RuleService.getBucketPrincipalKey(nowLimiterRule), id,
                nowLimiterRule.getUnit().toSeconds(nowLimiterRule.getInitialDelay()) + 1, TimeUnit.SECONDS);
        if (!result) {
            //该令牌桶已有线程负责存放
            String name = valueOperations.get(RuleService.getBucketPrincipalKey(nowLimiterRule));
            if (!id.equals(name)) {
                //而且还不是自己
                return;
            }
        }
        //分配向桶里存放令牌的任务
        if (taskMap.containsKey(RuleService.getBucketKey(nowLimiterRule))) {
            if (nowLimiterRule.getVersion() > oldLimiterRule.getVersion()) {
                ScheduledFuture scheduledFuture = taskMap.get(RuleService.getBucketKey(nowLimiterRule));
                scheduledFuture.cancel(true);
            } else {
                return;
            }
        }
        //执行任务
        ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            String rule = valueOperations.get(RuleService.getLimiterRuleKey(nowLimiterRule));
            if (rule == null || "".equals(rule)) {
                logger.debug("task cancel : " + RuleService.getLimiterRuleKey(nowLimiterRule));
                taskMap.get(RuleService.getBucketKey(nowLimiterRule)).cancel(true);
                taskMap.remove(RuleService.getBucketKey(nowLimiterRule));
                return;
            }
            valueOperations.set(RuleService.getBucketKey(nowLimiterRule), String.valueOf(nowLimiterRule.getLimit()),
                    nowLimiterRule.getUnit().toSeconds(nowLimiterRule.getPeriod()) + 1, TimeUnit.SECONDS);
        }, nowLimiterRule.getInitialDelay(), nowLimiterRule.getPeriod(), nowLimiterRule.getUnit());
        taskMap.put(RuleService.getBucketKey(nowLimiterRule), scheduledFuture);
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
            throw new TicketServerException(ResultEnum.ERROR, "规则不存在");
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

    @Override
    public Result<LimiterRule> getAllRule(String app, String id, int page, int limit) {
        Set<String> keys = redisTemplate.keys(RuleService.getLimiterRuleKeys(app, id));
        if (keys == null) {
            throw new TicketServerException(ResultEnum.ERROR, RuleService.getLimiterRuleKeys(app, id) + " return null");
        }
        //result
        Result<LimiterRule> result = new Result<>(ResultEnum.SUCCESS);
        result.setCount(keys.size());
        //list
        List<LimiterRule> limiterRules = new ArrayList<>();
        keys.stream().skip((page - 1) * limit).limit(limit)
                .forEach(s -> {
                    String s1 = valueOperations.get(s);
                    LimiterRule limiterRule = JSON.parseObject(s1, LimiterRule.class);
                    updateInstanceNumber(limiterRule);
                    limiterRules.add(limiterRule);
                });
        result.setData(limiterRules);
        return result;
    }
}
