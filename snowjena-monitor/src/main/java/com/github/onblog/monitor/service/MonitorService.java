package com.github.onblog.monitor.service;

import com.github.onblog.monitor.entity.MonitorBean;

import java.util.List;

public interface MonitorService {

    String PRE = "$PRE$";
    String AFTER = "$AFTER$";
    String DATE = "$DATA$";

    static String getMonitorPreKey(MonitorBean m){
        return PRE + m.getApp() + m.getId()+ DATE + m.getDateTime();
    }

    static String getMonitorAfterKey(MonitorBean m){
        return AFTER + m.getApp() + m.getId() + DATE + m.getDateTime();
    }

    static String getMonitorPreKeys(String app,String id){
        return PRE + app + id + "*";
    }

    static String getMonitorAfterKeys(String app,String id){
        return AFTER + app + id + "*";
    }

    /**
     * 保存监控数据
     */
    void save(List<MonitorBean> monitorBeans);

    /**
     * 获取监控数据
     */
    List<MonitorBean> getAll(String app, String id);


    boolean clean(String app, String id);
}
