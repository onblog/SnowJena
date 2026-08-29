package com.github.onblog.monitor.client;

import com.github.onblog.monitor.entity.MonitorBean;

import java.util.List;

/**
 * Create by Martin 2019/5/4 0004 12:50
 */
public interface MonitorService {

    /**
     * 获取并删除监控数据
     */
    List<MonitorBean> getAndDelete();

    /**
     * 保存一条监控数据
     */
    void save(MonitorBean monitorBean);

}
