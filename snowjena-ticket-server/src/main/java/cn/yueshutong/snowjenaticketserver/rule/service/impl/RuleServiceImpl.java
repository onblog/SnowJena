package cn.yueshutong.snowjenaticketserver.rule.service.impl;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.rule.entity.Result;
import cn.yueshutong.snowjenaticketserver.rule.service.RuleService;
import cn.yueshutong.redislock.RedisLock;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RuleServiceImpl implements RuleService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisLock lock;

    private ValueOperations<String, String> opsForValue;
    private Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);

    private static final String prefix = "$rule$";
    private static final String LOCK = "$LOCK$";

    @PostConstruct
    private void init() {
        opsForValue = redisTemplate.opsForValue();
    }

    @Override
    public LimiterRule heartbeat(LimiterRule limiterRule) {
        //读取最新的规则
        LimiterRule newRule = getNewRule(limiterRule);
        //检测实例数量，更新QPS
        updateQps(newRule);
        //延长过期时间
        opsForValue.set(getKey(newRule), JSON.toJSONString(newRule), 5, TimeUnit.SECONDS);
        return newRule;
    }

    /**
     * 读取最新的规则
     */
    private LimiterRule getNewRule(LimiterRule limiterRule) {
        Set<String> keys = redisTemplate.keys(getKeys(limiterRule));
        List<LimiterRule> limiterRules = new ArrayList<>();
        keys.forEach(s -> {
            String s1 = opsForValue.get(s);
            LimiterRule limiter = JSON.parseObject(s1, LimiterRule.class);
            limiterRules.add(limiter);
        });
        if (limiterRules.isEmpty()) {
            limiterRules.add(limiterRule);
        } else {
            Collections.sort(limiterRules);
        }
        String rule = opsForValue.get(getKey(limiterRules.get(limiterRules.size() - 1)));
        if (rule != null && !"".equals(rule)) {
            LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
            if (limiter.getVersion() > limiterRule.getVersion()) {
                limiter.setName(limiterRule.getName());
                return limiter;
            }
        }
        return limiterRule;
    }

    /**
     * 更新实例的QPS
     */
    private void updateQps(LimiterRule limiterRule) {
        //计算实例数量
        String key = getKey(limiterRule);
        Set<String> keys = redisTemplate.keys(prefix + limiterRule.getApp() + limiterRule.getId() + "*");
        assert keys != null;
        int size = keys.size();
        if (!keys.contains(key)) {
            size++;
        }
        //更新实例数
        limiterRule.setNumber(size);
        //检查QPS是否变化
        double qps = limiterRule.getAllQps() / size; //AllQps、Size不确定
        if (qps != limiterRule.getQps()) {
            limiterRule.setQps(qps);
            limiterRule.setVersion(limiterRule.getVersion() + 1);
            try {
                synchronized (this) {
                    //延时生效：使客户端错开1秒时间
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean update(LimiterRule limiterRule) {
        String key = getKey(limiterRule);
        String value = String.valueOf(System.currentTimeMillis());
        //加锁
        if (!lock.lock(LOCK + key, value)) {
            return false;
        }
        String rule = opsForValue.get(getKey(limiterRule));
        //实例不存在
        if (rule == null || "".equals(rule)) {
            return false;
        }
        //更新版本号
        LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
        limiterRule.setVersion(limiter.getVersion() + 1);
        //更新规则
        opsForValue.set(key, JSON.toJSONString(limiterRule), 5, TimeUnit.SECONDS); //3
        //解锁
        lock.unlock(key, value);
        return true;
    }

    @Override
    public Result<LimiterRule> getAll(String app, String id, String name, int page, int limit) {
        String builder = (app == null ? "" : app) +
                (id == null ? "" : id) +
                (name == null ? "" : name);
        Set<String> keys = redisTemplate.keys(prefix + builder);
        assert keys != null;
        //result
        Result<LimiterRule> result = new Result<>();
        result.setCount(keys.size());
        //list
        List<LimiterRule> limiterRules = new ArrayList<>();
        keys.stream().skip((page - 1) * limit).limit(limit)
                .forEach(s -> {
                    String s1 = opsForValue.get(s);
                    LimiterRule limiterRule = JSON.parseObject(s1, LimiterRule.class);
                    limiterRules.add(limiterRule);
                });
        //合并相同实例
        Map<String, List<String>> map = new HashMap<>();
        limiterRules.forEach(s -> { //APP+ID: NAMES
            if (map.get(s.getApp() + s.getId()) == null) {
                List<String> strings = new ArrayList<>();
                map.put(s.getApp() + s.getId(), strings);
            }
            List<String> names = map.get(s.getApp() + s.getId());
            names.add(s.getName());
        });
        Iterator<LimiterRule> iterator = limiterRules.iterator();
        while (iterator.hasNext()){ //distinct
            LimiterRule next = iterator.next();
            if (map.containsKey(next.getApp()+next.getId())){
                next.setName(toArray(map.get(next.getApp()+next.getId())));
                map.remove(next.getApp()+next.getId());
            }else {
                iterator.remove();
            }
        }
        result.setData(limiterRules);
        return result;
    }

    private String getKey(LimiterRule limiterRule) {
        return prefix + limiterRule.getApp() + limiterRule.getId() + limiterRule.getName();
    }

    private String getKeys(LimiterRule limiterRule) {
        return prefix + limiterRule.getApp() + limiterRule.getId() + "*";
    }

    private String toArray(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            if (i>0){
                builder.append(",");
            }
            builder.append(strings.get(i));
        }
        return builder.toString();
    }
}
