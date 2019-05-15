package cn.yueshutong.currentlimitingticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RuleServiceImpl implements RuleService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private ValueOperations<String, String> opsForValue;
    private Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);

    private static final String prefix = "$rule$";

    @PostConstruct
    private void init() {
        opsForValue = redisTemplate.opsForValue();
    }

    @Override
    public LimiterRule check(LimiterRule limiterRule) {
        String key = prefix + limiterRule.getApp() + limiterRule.getId() + limiterRule.getName();
        Set<String> keys = redisTemplate.keys(prefix + limiterRule.getApp() + limiterRule.getId() + "*");//1
        assert keys != null;
        int size = keys.size();
        if (!keys.contains(key)) {
            size++;
        }
        logger.info(key + "规则" + limiterRule.getQps() + "实例" + size + "keys:" + keys.size());
        if (limiterRule.getAllqps() / size != limiterRule.getQps()) {
            limiterRule.setQps(limiterRule.getAllqps() / size);
            limiterRule.setVersion(limiterRule.getVersion() + 1);
        }
        opsForValue.set(key, JSON.toJSONString(limiterRule),5,TimeUnit.SECONDS); //2
        return limiterRule;
    }
}
