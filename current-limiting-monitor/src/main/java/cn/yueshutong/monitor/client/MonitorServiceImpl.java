package cn.yueshutong.monitor.client;

import cn.yueshutong.monitor.entity.MonitorBean;

import java.time.LocalDateTime;
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
                    map.put(time, new MonitorBean(LocalDateTime.now()));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void save(MonitorBean monitorBean){
        initMap(monitorBean.getData());
        MonitorBean bean = map.get(monitorBean.getData());
        lock.lock();
        try {
            bean.setPre(monitorBean.getPre() + bean.getPre());
            bean.setAfter(monitorBean.getAfter() + bean.getAfter());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public List<MonitorBean> getAndDelete() {
        List<MonitorBean> list = new ArrayList<>(map.values());
        list.sort(null);
        if (list.size()>1) {
            list.remove(list.size() - 1);
        }
        list.forEach(s -> map.remove(s.getData()));
        return list;
    }

}
