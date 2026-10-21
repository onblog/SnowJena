package io.github.onblog.limiter;

import java.time.Duration;

/**
 * Factory methods for the built-in limiters.
 *
 * <p>Every method takes a name, so a limiter can be identified in logs and metrics:
 *
 * <pre>{@code
 * RateLimiter perSecond = RateLimiters.tokenBucket("api", 100);                  // 100/s, bursts of 100
 * RateLimiter bursty    = RateLimiters.tokenBucket("api", 200, 100, Duration.ofSeconds(1)); // 100/s, bursts of 200
 * RateLimiter smooth    = RateLimiters.leakyBucket("upstream", 100);             // one permit every 10ms
 * RateLimiter rolling   = RateLimiters.slidingWindow("login", 5, Duration.ofMinutes(1));
 * RateLimiter cheapest  = RateLimiters.fixedWindow("reports", 100, Duration.ofHours(1));
 * }</pre>
 */
public final class RateLimiters {

    private RateLimiters() {
    }

    /**
     * Creates a token bucket limiter that allows {@code permitsPerSecond} on average with bursts of
     * up to {@code permitsPerSecond}.
     *
     * @param name             human-readable name used in logs and metrics
     * @param permitsPerSecond permits granted per second
     * @return a new limiter
     */
    public static TokenBucketLimiter tokenBucket(String name, int permitsPerSecond) {
        Checks.positive(permitsPerSecond, "permitsPerSecond");
        return new TokenBucketLimiter(name, permitsPerSecond, permitsPerSecond, Duration.ofSeconds(1));
    }

    /**
     * Creates a token bucket limiter with an explicit burst capacity.
     *
     * @param name             human-readable name used in logs and metrics
     * @param capacity         maximum burst, i.e. the most permits one request may ask for
     * @param permitsPerWindow permits granted per {@code window}
     * @param window           refill window
     * @return a new limiter
     */
    public static TokenBucketLimiter tokenBucket(String name, int capacity, int permitsPerWindow, Duration window) {
        return new TokenBucketLimiter(name, capacity, permitsPerWindow, window);
    }

    /**
     * Creates a leaky bucket limiter that grants {@code permitsPerSecond}, evenly spaced.
     *
     * @param name             human-readable name used in logs and metrics
     * @param permitsPerSecond permits granted per second
     * @return a new limiter
     */
    public static LeakyBucketLimiter leakyBucket(String name, int permitsPerSecond) {
        Checks.positive(permitsPerSecond, "permitsPerSecond");
        return new LeakyBucketLimiter(name, permitsPerSecond, Duration.ofSeconds(1));
    }

    /**
     * Creates a leaky bucket limiter that grants {@code permitsPerWindow}, evenly spaced.
     *
     * @param name             human-readable name used in logs and metrics
     * @param permitsPerWindow permits granted per {@code window}
     * @param window           window defining the pace
     * @return a new limiter
     */
    public static LeakyBucketLimiter leakyBucket(String name, int permitsPerWindow, Duration window) {
        return new LeakyBucketLimiter(name, permitsPerWindow, window);
    }

    /**
     * Creates a sliding window limiter with {@link SlidingWindowLimiter#DEFAULT_SLOTS} slots.
     *
     * @param name       human-readable name used in logs and metrics
     * @param maxPermits permits allowed inside one rolling window
     * @param window     length of the rolling window
     * @return a new limiter
     */
    public static SlidingWindowLimiter slidingWindow(String name, int maxPermits, Duration window) {
        return new SlidingWindowLimiter(name, maxPermits, window);
    }

    /**
     * Creates a fixed window limiter, the cheapest implementation.
     *
     * @param name       human-readable name used in logs and metrics
     * @param maxPermits permits allowed inside one window
     * @param window     length of the window
     * @return a new limiter
     */
    public static FixedWindowLimiter fixedWindow(String name, int maxPermits, Duration window) {
        return new FixedWindowLimiter(name, maxPermits, window);
    }
}
