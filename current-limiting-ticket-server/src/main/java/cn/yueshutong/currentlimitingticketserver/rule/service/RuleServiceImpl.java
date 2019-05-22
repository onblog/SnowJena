package cn.yueshutong.currentlimitingticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.redislock.RedisLock;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Override
    public boolean update(LimiterRule limiterRule) {
        String key = getKey(limiterRule);
        String value = String.valueOf(System.currentTimeMillis());
        //加锁
        if (!lock.lock(LOCK +key,value)){
            return false;
        }
        String rule = opsForValue.get(getKey(limiterRule));
        //实例不存在
        if (rule == null || "".equals(rule)) {
            return false;
        }
        //更新版本号
        LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
        limiterRule.setVersion(limiter.getVersion()+1);
        //更新规则
        opsForValue.set(key, JSON.toJSONString(limiterRule), 5, TimeUnit.SECONDS); //3
        //解锁
        lock.unlock(key,value);
        return true;
    }

    @Override
    public List<LimiterRule> getAll(String app, String id, String name) {
        String builder = (app == null ? "" : app) +
                (id == null ? "" : id) +
                (name == null ? "" : name);
        Set<String> keys = redisTemplate.keys(prefix+ builder +"*");
        assert keys != null;
        List<LimiterRule> limiterRules = new ArrayList<>();
        keys.forEach(s -> {
            String s1 = opsForValue.get(s);
            LimiterRule limiterRule = JSON.parseObject(s1, LimiterRule.class);
            limiterRules.add(limiterRule);
        });
        return limiterRules;
    }



    private String getKey(LimiterRule limiterRule) {
        return prefix + limiterRule.getApp() + limiterRule.getId() + limiterRule.getName();
    }

    private LimiterRule getNewRule(LimiterRule limiterRule) {
        String rule = opsForValue.get(getKey(limiterRule));
        if (rule != null && !"".equals(rule)) {
            LimiterRule limiter = JSON.parseObject(rule, LimiterRule.class);
            if (limiter.getVersion() > limiterRule.getVersion()) {
                return limiter;
            }
        }
        return limiterRule;
    }

    private void updateQps(LimiterRule limiterRule) {
        String key = getKey(limiterRule);
        Set<String> keys = redisTemplate.keys(prefix + limiterRule.getApp() + limiterRule.getId() + "*");
        assert keys != null;
        int size = keys.size();
        if (!keys.contains(key)) {
            size++;
        }
        logger.debug(key + " 规则" + limiterRule.getQps() + " 实例" + size + "keys:" + keys.size());
        if (limiterRule.getAllqps() / size != limiterRule.getQps()) {
            limiterRule.setQps(limiterRule.getAllqps() / size);
            limiterRule.setVersion(limiterRule.getVersion() + 1);
        }
    }


}
