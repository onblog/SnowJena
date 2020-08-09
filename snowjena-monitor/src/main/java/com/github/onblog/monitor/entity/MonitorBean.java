package com.github.onblog.monitor.entity;

import com.github.onblog.monitor.common.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 12:51
 */
public class MonitorBean implements Comparable<MonitorBean>{
    private String app;
    private String id;
    private String name;
    private long monitor;
    private int pre;
    private int after;
    private String time;
    private String dateTime;
    private LocalDateTime localDateTime;

    public MonitorBean() {
    }

    public MonitorBean(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public long getMonitor() {
        return monitor;
    }

    public void setMonitor(long monitor) {
        this.monitor = monitor;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
    @Override
    public int compareTo(MonitorBean o) {
        return this.getLocalDateTime().compareTo(o.getLocalDateTime());
    }


    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPre() {
        return pre;
    }

    public void setPre(int pre) {
        this.pre = pre;
    }

    public int getAfter() {
        return after;
    }

    public void setAfter(int after) {
        this.after = after;
    }


    public String getTime() {
        return localDateTime!=null?localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")):"";
    }

    public String getDateTime() {
        return DateTimeUtil.toString(localDateTime);
    }

    public void setTime(String time) {
        this.time = time;
    }


    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
