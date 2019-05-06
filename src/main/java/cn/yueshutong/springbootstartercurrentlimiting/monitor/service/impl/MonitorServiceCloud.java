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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    ValueOperations<String, String> opsForValue;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String APP = SpringContextUtil.getApplicationName();
    private static final String KEYS = APP + "_keys";
    private static final String PRE = "pre";
    private static final String AFTER = "after";

    /**
     * Key:APP+PRE+time
     * value:i(++)
     * setnx+expire
     */

    @PostConstruct
    private void init() {
        opsForValue = template.opsForValue();
    }

    @Override
    public void savePre(String time) {
        String key = APP + PRE + time;
        opsForValue.setIfAbsent(key, String.valueOf(0), monitorProperties.getTime(), TimeUnit.SECONDS);
        opsForValue.increment(key);
    }

    @Override
    public void saveAfter(String time) {
        String key = APP + AFTER + time;
        opsForValue.setIfAbsent(key, String.valueOf(0), monitorProperties.getTime(), TimeUnit.SECONDS);
        opsForValue.increment(key);
    }

    @Override
    public List<MonitorBean> queryAll() {
        List<MonitorBean> list = new ArrayList<>();
        Set<String> pres = template.keys(APP + PRE + "*");
        Set<String> keys = template.keys(APP + AFTER + "*");
        pres.forEach(k -> {
            String time = k.substring(APP.length() + PRE.length());
            MonitorBean monitorBean = new MonitorBean();
            monitorBean.setLocalDateTime(DateTime.parse(time));
            String pre = template.opsForValue().get(k);
            if (pre==null){
                return;
            }
            monitorBean.setPre(Integer.valueOf(pre));
            String after = template.opsForValue().get(APP + AFTER + time);
            if (after==null){
                return;
            }
            monitorBean.setAfter(Integer.valueOf(after));
            list.add(monitorBean);
        });
        list.sort(null);
        return list;
    }

}