package cn.yueshutong.monitor.entity;

import cn.yueshutong.monitor.common.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 12:51
 */
public class MonitorBean implements Comparable<MonitorBean>{
    private String app;
    private String id;
    private String name;
    private int pre;
    private int after;
    private String time;
    private String date;
    private LocalDateTime localDateTime;

    public MonitorBean() {
    }

    public MonitorBean(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
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

    public String getDate() {
        return DateTimeUtil.toString(localDateTime);
    }

    public void setTime(String time) {
        this.time = time;
    }


    public void setDate(String date) {
        this.date = date;
    }
}
