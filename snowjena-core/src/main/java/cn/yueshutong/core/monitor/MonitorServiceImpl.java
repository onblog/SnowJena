package cn.yueshutong.core.monitor;

import cn.yueshutong.monitor.client.MonitorService;
import cn.yueshutong.monitor.common.DateTimeUtil;
import cn.yueshutong.monitor.entity.MonitorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MonitorServiceImpl implements MonitorService {
    private Map<String, MonitorBean> map = new ConcurrentHashMap<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Logger logger = LoggerFactory.getLogger(MonitorService.class);

    private void initMap(String time) {
        if (!map.containsKey(time)) {
            lock.writeLock().lock();
            try {
                if (!map.containsKey(time)) {
                    map.put(time, new MonitorBean(DateTimeUtil.parse(time)));
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void save(MonitorBean monitorBean) {
        initMap(monitorBean.getDateTime());
        lock.readLock().lock();
        try {
            MonitorBean bean = map.get(monitorBean.getDateTime());
            bean.setApp(monitorBean.getApp());
            bean.setId(monitorBean.getId());
            bean.setName(monitorBean.getName());
            bean.setPre(monitorBean.getPre() + bean.getPre());
            bean.setAfter(monitorBean.getAfter() + bean.getAfter());
            bean.setMonitor(monitorBean.getMonitor());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<MonitorBean> getAndDelete() {
        HashMap<String, MonitorBean> beanHashMap = new HashMap<>(map);
        List<MonitorBean> list = new ArrayList<>(beanHashMap.values());
        list.sort(null);
        if (list.size() > 1) {
            list.remove(list.size() - 1);
        }
        for (MonitorBean monitorBean : list) {
            lock.writeLock().lock();
            try {
                map.remove(monitorBean.getDateTime());
            } finally {
                lock.writeLock().unlock();
            }
        }
        return list;
    }

}
