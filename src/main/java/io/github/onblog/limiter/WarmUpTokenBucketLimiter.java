package io.github.onblog.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token bucket limiter with a warm-up period: starts at a fraction of the configured rate and ramps up
 * to it. The right choice for a limiter that guards a dependency which is itself cold — an empty cache,
 * an un-JITed code path, a freshly started pod.
 *
 * <p>During the warm-up period the effective rate grows linearly from {@code permits / coldFactor} to
 * {@code permits}, exactly the behaviour of Alibaba Sentinel's warm-up controller: the cold factor is
 * fixed at {@value #COLD_FACTOR}, so a limiter configured for 300 per second admits 100 per second at
 * start-up and reaches the full 300 after {@code warmUpPeriod}. A quiet limiter is not warmed up again
 * by idling — the ramp depends only on the age of the limiter.
 *
 * <p>Because the emission interval is longer while cold, a single request cannot spend the full burst
 * capacity either: the burst budget is {@code capacity} intervals, so only {@code capacity / coldFactor}
 * permits are available at start-up. Both the rate and the burst therefore grow together, which is what
 * makes the ramp gentle for the dependency rather than merely slower on average.
 *
 * <pre>{@code
 * // Full speed 600/min, but only 200/min for the first two minutes after start-up.
 * RateLimiter limiter = RateLimiters.warmUpTokenBucket("search", 600, Duration.ofMinutes(2));
 * }</pre>
 *
 * <p>The rate is derived from {@link System#nanoTime()} on demand, so there is no scheduler thread.
 * Instances are thread-safe and safe to share across threads.
 */
public final class WarmUpTokenBucketLimiter implements RateLimiter {

    /** How much slower the limiter runs at t=0: one third of the configured rate, as in Sentinel. */
    public static final int COLD_FACTOR = 3;

    private final String name;
    private final int capacity;
    private final int permitsPerWindow;
    private final long steadyEmissionNanos;
    private final long toleranceNanos;
    private final long warmUpNanos;
    private final long startNanos;

    /** Time at which the next permit becomes available; a single CAS-protected value. */
    private final AtomicLong theoreticalArrivalNanos;

    /**
     * Creates a warm-up token bucket limiter.
     *
     * @param name             human-readable name used in logs and metrics
     * @param capacity         maximum burst once the limiter is warm
     * @param permitsPerWindow permits granted per {@code window} once the limiter is warm
     * @param window           refill window at full speed
     * @param warmUpPeriod     how long the ramp takes; must be positive
     * @throws IllegalArgumentException if any value is not positive or the period is missing
     */
    public WarmUpTokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window,
                                    Duration warmUpPeriod) {
        this.name = Checks.name(name);
        this.capacity = Checks.positive(capacity, "capacity");
        this.permitsPerWindow = Checks.positive(permitsPerWindow, "permitsPerWindow");
        long windowNanos = Checks.nanos(window, "window");
        this.warmUpNanos = Checks.nanos(warmUpPeriod, "warmUpPeriod");
        this.steadyEmissionNanos = Math.max(1L, windowNanos / permitsPerWindow);
        this.toleranceNanos = Checks.multiply(steadyEmissionNanos, capacity, "burst tolerance");
        this.startNanos = System.nanoTime();
        this.theoreticalArrivalNanos = new AtomicLong(startNanos);
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the burst capacity once the limiter is warm */
    public int capacity() {
        return capacity;
    }

    /** @return the permits per window once the limiter is warm */
    public int permitsPerWindow() {
        return permitsPerWindow;
    }

    /** @return the cold factor, always {@value #COLD_FACTOR} */
    public int coldFactor() {
        return COLD_FACTOR;
    }

    /** @return how far the ramp has progressed, from {@code 0} to {@code 1} */
    public double warmUpProgress() {
        long elapsed = System.nanoTime() - startNanos;
        if (elapsed <= 0) {
            return 0d;
        }
        return elapsed >= warmUpNanos ? 1d : (double) elapsed / (double) warmUpNanos;
    }

    @Override
    public int availablePermits() {
        long now = System.nanoTime();
        long interval = emissionNanos(now);
        long tolerance = toleranceNanos(interval);
        long ahead = theoreticalArrivalNanos.get() - now;
        long usable = tolerance - (ahead > 0 ? ahead : 0);
        if (usable <= 0) {
            return 0;
        }
        long permits = usable / interval;
        return (int) Math.min(permits, capacity);
    }

    @Override
    public boolean tryAcquire(int permits) {
        Checks.positive(permits, "permits");
        long now = System.nanoTime();
        long interval = emissionNanos(now);
        long tolerance = toleranceNanos(interval);
        long step = permits * interval;
        if (step > tolerance) {
            return false; // larger than the burst budget currently allows
        }
        while (true) {
            long arrival = theoreticalArrivalNanos.get();
            long ahead = arrival - now;
            if (ahead <= 0) {
                if (theoreticalArrivalNanos.compareAndSet(arrival, now + step)) {
                    return true;
                }
            } else if (ahead + step <= tolerance) {
                if (theoreticalArrivalNanos.compareAndSet(arrival, arrival + step)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Burst budget in nanoseconds for the given emission interval.
     *
     * <p>Normally the budget is {@code capacity} steady intervals, which while cold holds only
     * {@code capacity / coldFactor} permits. That would starve a limiter whose capacity is smaller than
     * the cold factor — a capacity of one would admit nothing at all until the ramp finished — so the
     * budget never drops below one emission interval: at least a single permit always fits.
     */
    private long toleranceNanos(long interval) {
        return Math.max(toleranceNanos, interval);
    }

    /**
     * Emission interval at {@code now}: the steady interval scaled by a factor that falls linearly from
     * {@value #COLD_FACTOR} to 1 over the warm-up period.
     */
    private long emissionNanos(long now) {
        long elapsed = now - startNanos;
        if (elapsed >= warmUpNanos) {
            return steadyEmissionNanos;
        }
        if (elapsed < 0) {
            elapsed = 0;
        }
        double progress = (double) elapsed / (double) warmUpNanos;
        double factor = COLD_FACTOR - (COLD_FACTOR - 1) * progress;
        return Math.max(1L, (long) (steadyEmissionNanos * factor));
    }

    @Override
    public String toString() {
        return "WarmUpTokenBucketLimiter{name='" + name + "', capacity=" + capacity
                + ", permitsPerWindow=" + permitsPerWindow
                + ", progress=" + String.format("%.2f", warmUpProgress()) + '}';
    }
}
