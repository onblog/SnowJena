package cn.yueshutong.currentlimitingticketserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 19:03
 */
@Component
@ConfigurationProperties(prefix = "current.limiting.monitor")
public class CurrentMonitorProperties {
    /**
     * Continuous monitoring duration, unit/second.
     */
    private long time = 60*30;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
