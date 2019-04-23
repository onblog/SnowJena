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
     * Is the local current limiter on?
     */
    private boolean partEnabled = true;

    /**
     * Do you want to turn on the cluster current limiter?
     */
    private boolean cloudEnabled = false;

    /**
     * application qps
     */
    private long qps = 100;

    /**
     * Initialization delay time
     */
    private long initialDelay = 0;

    /**
     * When the token is empty, does it fail fast or block?
     */
    private boolean failFast = true;

    public boolean isEnabled() {
        return enabled;
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

    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
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

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }
}
