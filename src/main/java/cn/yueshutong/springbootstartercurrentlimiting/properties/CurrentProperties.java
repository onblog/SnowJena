package cn.yueshutong.springbootstartercurrentlimiting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by yster@foxmail.com 2019/4/21 0021 11:19
 */
@Component
@ConfigurationProperties(prefix = "current.limiting")
public class CurrentProperties {
    /**
     * Do you want to turn on the current limiting ?
     */
    private boolean enabled = false;

    /**
     * Do you want to turn on the annotation current limiter?
     */
    private boolean partEnabled = true;

    /**
     * Do you want to turn on the cluster current limiter?
     */
    private boolean cloudEnabled = false;

    /**
     * application qps：The number/SEC
     */
    private double qps = 100;

    /**
     * The delay time for the token to be put in for the first time.
     */
    private long initialDelay = 0;

    /**
     * When the token is empty, does it fail fast or block?
     */
    private boolean failFast = true;

    /**
     * Are request rates and frequency strictly controlled?
     */
    private boolean overflow = false;

    /**
     * How many seconds will it take to reclaim the expired limiter object?
     */
    private long recycling = 10;

    /**
     * The number of core threads in the thread pool performing the scheduled task.
     */
    private int corePoolSize = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPartEnabled() {
        return partEnabled;
    }

    public void setPartEnabled(boolean partEnabled) {
        this.partEnabled = partEnabled;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public long getRecycling() {
        return recycling;
    }

    public void setRecycling(long recycling) {
        this.recycling = recycling;
    }

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }
}
