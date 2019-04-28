package cn.yueshutong.springbootstartercurrentlimiting.property;

import java.time.temporal.ChronoUnit;

public class CurrentProperty {
    private String id;
    private double qps;
    private long initialDelay;
    private boolean failFast;
    private boolean overflow;
    private long time;
    private ChronoUnit unit;

    CurrentProperty(String id, double qps, long initialDelay, boolean failFast, boolean overflow, long time, ChronoUnit unit) {
        this.id = id;
        this.qps = qps;
        this.initialDelay = initialDelay;
        this.failFast = failFast;
        this.overflow = overflow;
        this.time = time;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public double getQps() {
        return qps;
    }

    public long getTime() {
        return time;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public boolean isFailFast() {
        return failFast;
    }

}
