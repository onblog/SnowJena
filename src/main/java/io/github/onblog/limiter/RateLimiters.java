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
 *
 * // Caps how many requests run at once, not how many per second.
 * ConcurrencyLimiter inFlight = RateLimiters.concurrency("db-pool", 20);
 *
 * // Full speed after two minutes, one third of the rate before that.
 * RateLimiter warming   = RateLimiters.warmUpTokenBucket("search", 600, Duration.ofMinutes(2));
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
     * Creates a concurrency limiter that caps how many permits may be held at the same time.
     *
     * <p>A permit is a slot and must be returned, so prefer the try-with-resources form
     * {@link ConcurrencyLimiter#tryEnter()} over a bare {@code tryAcquire()}.
     *
     * @param name           human-readable name used in logs and metrics
     * @param maxConcurrency maximum number of permits held at once
     * @return a new limiter
     */
    public static ConcurrencyLimiter concurrency(String name, int maxConcurrency) {
        return new ConcurrencyLimiter(name, maxConcurrency);
    }

    /**
     * Creates a warm-up token bucket limiter that allows {@code permitsPerSecond} once warm and one
     * third of it at start-up, ramping up linearly over {@code warmUpPeriod}.
     *
     * @param name             human-readable name used in logs and metrics
     * @param permitsPerSecond permits granted per second once warm
     * @param warmUpPeriod     how long the ramp takes
     * @return a new limiter
     */
    public static WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int permitsPerSecond,
                                                             Duration warmUpPeriod) {
        Checks.positive(permitsPerSecond, "permitsPerSecond");
        return new WarmUpTokenBucketLimiter(name, permitsPerSecond, permitsPerSecond,
                Duration.ofSeconds(1), warmUpPeriod);
    }

    /**
     * Creates a warm-up token bucket limiter with an explicit burst capacity.
     *
     * @param name             human-readable name used in logs and metrics
     * @param capacity         burst capacity once warm
     * @param permitsPerWindow permits granted per {@code window} once warm
     * @param window           refill window at full speed
     * @param warmUpPeriod     how long the ramp takes
     * @return a new limiter
     */
    public static WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int capacity, int permitsPerWindow,
                                                             Duration window, Duration warmUpPeriod) {
        return new WarmUpTokenBucketLimiter(name, capacity, permitsPerWindow, window, warmUpPeriod);
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
