package cn.yueshutong.monitor.client;

import cn.yueshutong.monitor.common.DateTimeUtil;
import cn.yueshutong.monitor.entity.MonitorBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorServiceImpl implements MonitorService {
    private Map<String, MonitorBean> map = new ConcurrentHashMap<>();
    private ReentrantLock lock = new ReentrantLock();

    private void initMap(String time) {
        if (!map.containsKey(time)) {
            lock.lock();
            try {
                if (!map.containsKey(time)) {
                    map.put(time, new MonitorBean(DateTimeUtil.parse(time)));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void save(MonitorBean monitorBean) {
        initMap(monitorBean.getKey());
        lock.lock();
        try {
            MonitorBean bean = map.get(monitorBean.getKey());
            bean.setApp(monitorBean.getApp());
            bean.setId(monitorBean.getId());
            bean.setName(monitorBean.getName());
            bean.setPre(monitorBean.getPre() + bean.getPre());
            bean.setAfter(monitorBean.getAfter() + bean.getAfter());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<MonitorBean> getAndDelete() {
        List<MonitorBean> list = new ArrayList<>(map.values());
        list.sort(null);
        if (list.size() > 1) {
            list.remove(list.size() - 1);
        }
        list.forEach(s -> map.remove(s.getKey()));
        return list;
    }

}
