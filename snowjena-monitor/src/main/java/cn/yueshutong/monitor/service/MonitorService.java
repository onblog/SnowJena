package cn.yueshutong.monitor.service;

import cn.yueshutong.monitor.entity.MonitorBean;

import java.util.List;

public interface MonitorService {

    /**
     * 保存监控数据
     */
    void save(List<MonitorBean> monitorBeans);

    /**
     * 获取监控数据
     */
    List<MonitorBean> getAll(String app, String id, String name);

}
