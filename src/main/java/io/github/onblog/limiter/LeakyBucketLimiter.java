package io.github.onblog.limiter;

import java.time.Duration;

/**
 * Leaky bucket limiter: releases permits at a smooth, even pace and never accumulates a burst.
 *
 * <p>Where a {@link TokenBucketLimiter} lets a quiet client spend a whole bucket at once, a leaky
 * bucket behaves like a queue that drains at a constant speed: after one permit is granted, the next
 * one becomes available only after one emission interval, regardless of how long the limiter was
 * idle. That is exactly a token bucket with a capacity of one permit, so both classes share the
 * same lock-free virtual-scheduling core and differ only in their burst tolerance.
 *
 * <p>Use it to pace outbound calls or message consumption, where bursts would overload the
 * downstream system. Use {@link TokenBucketLimiter} when bursts are acceptable.
 *
 * <pre>{@code
 * // Exactly one permit every 10ms, no bursts.
 * RateLimiter limiter = new LeakyBucketLimiter("upstream-api", 100, Duration.ofSeconds(1));
 * }</pre>
 *
 * <p>Instances are thread-safe and safe to share across threads.
 */
public final class LeakyBucketLimiter implements RateLimiter {

    private final String name;
    private final int permitsPerWindow;
    private final Gcra schedule;

    /**
     * Creates a leaky bucket limiter.
     *
     * @param name             human-readable name used in logs and metrics
     * @param permitsPerWindow permits granted per {@code window}, evenly spaced
     * @param window           window defining the pace, for example {@code Duration.ofSeconds(1)}
     * @throws IllegalArgumentException if any value is not positive
     */
    public LeakyBucketLimiter(String name, int permitsPerWindow, Duration window) {
        this.name = Checks.name(name);
        this.permitsPerWindow = Checks.positive(permitsPerWindow, "permitsPerWindow");
        // A capacity of one permit means no burst is ever accumulated.
        this.schedule = new Gcra(permitsPerWindow, window, 1);
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the configured permits per window */
    public int permitsPerWindow() {
        return permitsPerWindow;
    }

    @Override
    public int availablePermits() {
        return schedule.availablePermits();
    }

    @Override
    public boolean tryAcquire(int permits) {
        // The bucket holds a single permit, so asking for more than one can never succeed.
        return schedule.tryAcquire(permits);
    }

    @Override
    public String toString() {
        return "LeakyBucketLimiter{name='" + name + "', permitsPerWindow=" + permitsPerWindow + '}';
    }
}
