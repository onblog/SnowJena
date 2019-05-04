package cn.yueshutong.springbootstartercurrentlimiting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 19:03
 */
@Component
@ConfigurationProperties(prefix = "current.limiting.monitor")
public class CurrentMonitorProperties {
    /**
     * Monitor your application requests and responses.
     */
    private boolean enabled = false;

    /**
     * Continuous monitoring duration, unit/second.
     */
    private long time = 60*60;

    /**
     * The monitoring module USES the Redis database.
     */
    private boolean inRedis = false;

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

    public boolean isInRedis() {
        return inRedis;
    }

    public void setInRedis(boolean inRedis) {
        this.inRedis = inRedis;
    }
}
