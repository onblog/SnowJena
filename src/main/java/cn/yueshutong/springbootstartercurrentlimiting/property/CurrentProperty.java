package cn.yueshutong.springbootstartercurrentlimiting.property;

public class CurrentProperty {
    private String id;
    private double qps;
    private long initialDelay;
    private boolean failFast;
    private boolean overflow;

    /**
     *
     * @param id 标识名，若为IP地址则为IP地址限流，若为用户名则为用户限流，若为访问的URL则为接口限流。
     * @param qps 每秒并发量，比如1分种允许100次调用，那么可以写：100/60。
     * @param initialDelay 首次放入令牌延迟时间，毫秒。
     * @param failFast 是否需开启快速失败。
     * @param overflow 是否严格控制请求速率和次数。
     */
    public CurrentProperty(String id, double qps, long initialDelay, boolean failFast, boolean overflow) {
        this.id = id;
        this.qps = qps;
        this.initialDelay = initialDelay;
        this.failFast = failFast;
        this.overflow = overflow;
    }

    public String getId() {
        return id;
    }

    public double getQps() {
        return qps;
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
