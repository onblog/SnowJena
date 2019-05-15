package cn.yueshutong.springbootstartercurrentlimiting.monitor.service.impl;

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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 13:04
 */
@Service
@ConditionalOnBean(MonitorInterceptor.class)
@ConditionalOnProperty(prefix = "current.limiting.monitor", name = "in-redis", havingValue = "false",matchIfMissing = true)
public class MonitorServiceSingle implements MonitorService {
    @Autowired
    private CurrentMonitorProperties monitorProperties;

    private Map<String, MonitorBean> map = new ConcurrentHashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() {
        this.recyle();
    }

    @Override
    public List<MonitorBean> queryAll() {
        List<MonitorBean> list = new ArrayList(map.values());
        list.sort(null);
        return list;
    }

    @Override
    public void savePre(String time) {
        initMapEntity(time);
        MonitorBean monitorBean = map.get(time);
        lock.lock();
        try {
            int pre = monitorBean.getPre();
            monitorBean.setPre(pre + 1);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void saveAfter(String time) {
        MonitorBean monitorBean = map.get(time);
        lock.lock();
        try {
            int after = monitorBean.getAfter();
            monitorBean.setAfter(after + 1);
        } finally {
            lock.unlock();
        }
    }

    private void initMapEntity(String time) {
        if (!map.containsKey(time)) {
            lock.lock();
            try {
                if (!map.containsKey(time)) {
                    map.put(time, new MonitorBean(LocalDateTime.now()));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void recyle() {
        ThreadPool.scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.debug("recyle...");
                Iterator<Map.Entry<String, MonitorBean>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, MonitorBean> e = iterator.next();
                    LocalDateTime time = e.getValue().getLocalDateTime();
                    if (time.plus(monitorProperties.getTime(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        iterator.remove();
                    }
                }
            }
        }, monitorProperties.getTime(), 3, TimeUnit.SECONDS);
    }

}
