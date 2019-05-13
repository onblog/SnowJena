package cn.yueshutong.springbootstartercurrentlimiting.monitor.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 12:51
 */
public class MonitorBean implements Comparable<MonitorBean>{
    private LocalDateTime localDateTime;
    private int pre;
    private int after;
    private String time;

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
        return this.localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public int compareTo(MonitorBean o) {
        return this.getLocalDateTime().compareTo(o.getLocalDateTime());
    }



}
