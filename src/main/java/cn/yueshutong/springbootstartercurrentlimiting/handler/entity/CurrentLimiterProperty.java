package cn.yueshutong.springbootstartercurrentlimiting.handler.entity;

public class CurrentLimiterProperty {
    private String id;
    private long qps;
    private long initialDelay;
    private boolean failFast;

    public CurrentLimiterProperty(String id, long qps, long initialDelay, boolean failFast) {
        this.id = id;
        this.qps = qps;
        this.initialDelay = initialDelay;
        this.failFast = failFast;
    }

    public String getId() {
        return id;
    }

    public long getQps() {
        return qps;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public boolean isFailFast() {
        return failFast;
    }
}
