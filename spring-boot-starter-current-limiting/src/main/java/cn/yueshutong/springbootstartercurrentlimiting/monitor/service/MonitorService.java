package cn.yueshutong.springbootstartercurrentlimiting.monitor.service;

import cn.yueshutong.springbootstartercurrentlimiting.monitor.entity.MonitorBean;

import java.util.List;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 12:50
 */
public interface MonitorService {

    List<MonitorBean> queryAll();

    void savePre(String time);

    void saveAfter(String time);

}
