package cn.yueshutong.springbootstartercurrentlimiting.monitor.service.impl;

import cn.yueshutong.springbootstartercurrentlimiting.common.DateTime;
import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.common.ThreadPool;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.MonitorInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.entity.MonitorBean;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.service.MonitorService;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentMonitorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 13:36
 */
@Service
@ConditionalOnBean(MonitorInterceptor.class)
@ConditionalOnProperty(prefix = "current.limiting.monitor", name = "in-redis", havingValue = "true")
public class MonitorServiceCloud implements MonitorService {
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private CurrentMonitorProperties monitorProperties;

    HashOperations<String, Object, Object> opsForHash;
    SetOperations<String, String> opsForSet;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String APP = SpringContextUtil.getApplicationName();
    private static final String KEYS = APP + "_keys";
    private static final String PRE = "pre";
    private static final String AFTER = "after";

    @PostConstruct
    private void init() {
        if (template == null) {
            throw new NullPointerException("你的RedisTemplate不可用！");
        }
        opsForHash = template.opsForHash();
        opsForSet = template.opsForSet();
        this.recyle();
    }

    @Override
    public List<MonitorBean> queryAll() {
        Set<String> members = opsForSet.members(KEYS);
        List<MonitorBean> list = new ArrayList<>();
        members.forEach(k -> {
            MonitorBean monitorBean = new MonitorBean();
            monitorBean.setLocalDateTime(DateTime.parse(k.substring(APP.length())));
            monitorBean.setPre(Integer.valueOf(opsForHash.get(k, PRE).toString()));
            monitorBean.setAfter(Integer.valueOf(opsForHash.get(k, AFTER).toString()));
            list.add(monitorBean);
        });
        return list;
    }

    @Override
    public void savePre(String time) {
        String key = APP + time; // key = now time
        opsForHash.putIfAbsent(key, PRE, 0); // init
        opsForHash.increment(key, PRE, 1); // i++
        opsForSet.add(KEYS, key); //keys
    }

    @Override
    public void saveAfter(String time) {
        String key = APP + time; // key = now time
        opsForHash.putIfAbsent(key, AFTER, 0); // time
        opsForHash.increment(key, AFTER, 1); //i++
    }

    private void recyle() {
        ThreadPool.scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.info("recyle..");
                Set<String> members = opsForSet.members(KEYS);
                members.forEach(s -> {
                    String time = s.substring(APP.length());
                    LocalDateTime parse = DateTime.parse(time);
                    if (parse.plus(monitorProperties.getTime(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        opsForSet.remove(KEYS, s);
                        opsForHash.delete(s);
                    }
                });
            }
        }, monitorProperties.getTime(), 10, TimeUnit.SECONDS);
    }

}