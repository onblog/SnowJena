package cn.yueshutong.springbootstartercurrentlimiting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by yster@foxmail.com 2019/4/28 0028 15:10
 */
@Component
@ConfigurationProperties(prefix = "current.limiting.rule")
public class CurrentRuleProperties {
    /**
     * application qps: The number/SEC
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
     * Whether to switch to leaky bucket algorithm?
     */
    private boolean overflow = false;

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

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }
}
