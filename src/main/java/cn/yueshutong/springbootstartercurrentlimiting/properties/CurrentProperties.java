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
     * The number of rateLimiter threads in the thread pool performing the scheduled task.
     */
    private int corePoolSize = 10;

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

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }

}
