package cn.yueshutong.springbootstartercurrentlimiting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 19:05
 */
@Component
@ConfigurationProperties(prefix = "current.limiting.recyle")
public class CurrentRecycleProperties {
    /**
     * Monitor your application requests and responses.
     */
    private boolean enabled = false;

    /**
     * How many seconds will it take to reclaim the expired limiter object?
     */
    private long time = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
