package io.github.onblog.limiter;

import java.time.Duration;

/**
 * Token bucket limiter: enforces an average rate while allowing a limited burst.
 *
 * <p>A bucket of {@code capacity} permits is filled at the configured rate. Every accepted request
 * takes permits out of the bucket; an empty bucket means the request is rejected. Because permits
 * accumulate while the limiter is idle, a client that has been quiet can spend up to
 * {@code capacity} permits at once — the classic "average rate with burst tolerance" behaviour.
 *
 * <p>Set {@code capacity == permitsPerWindow} for a limiter that may spend one full window of
 * permits at once, or {@code capacity == 1} when bursts are undesired (see
 * {@link LeakyBucketLimiter}, which is the same rate control with a capacity of one).
 *
 * <p>The bucket is refilled lazily from {@link System#nanoTime()} when a request arrives; there is
 * no background thread and no call allocation.
 *
 * <pre>{@code
 * // Up to 100 requests per second on average, bursts of up to 200.
 * RateLimiter limiter = new TokenBucketLimiter("payments", 200, 100, Duration.ofSeconds(1));
 *
 * if (limiter.tryAcquire()) {
 *     handleRequest();
 * } else {
 *     response.setStatus(429);
 * }
 * }</pre>
 *
 * <p>Instances are thread-safe and safe to share across threads.
 */
public final class TokenBucketLimiter implements RateLimiter {

    private final String name;
    private final int capacity;
    private final Gcra schedule;

    /**
     * Creates a token bucket limiter.
     *
     * @param name            human-readable name used in logs and metrics
     * @param capacity        maximum permits the bucket can hold, i.e. the largest single burst
     * @param permitsPerWindow permits refilled per {@code window}
     * @param window          refill window, for example {@code Duration.ofSeconds(1)}
     * @throws IllegalArgumentException if any value is not positive
     */
    public TokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window) {
        this.name = Checks.name(name);
        this.capacity = Checks.positive(capacity, "capacity");
        this.schedule = new Gcra(permitsPerWindow, window, capacity);
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the burst capacity, i.e. the largest number of permits one request may ask for */
    public int capacity() {
        return capacity;
    }

    @Override
    public int availablePermits() {
        return schedule.availablePermits();
    }

    @Override
    public boolean tryAcquire(int permits) {
        return schedule.tryAcquire(permits);
    }

    @Override
    public String toString() {
        return "TokenBucketLimiter{name='" + name + "', capacity=" + capacity + '}';
    }
}
