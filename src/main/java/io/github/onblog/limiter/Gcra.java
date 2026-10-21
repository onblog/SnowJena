package io.github.onblog.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Virtual-scheduling rate control, also known as the Generic Cell Rate Algorithm (GCRA), shared by
 * {@link TokenBucketLimiter} and {@link LeakyBucketLimiter}.
 *
 * <p>Instead of storing a token counter that a scheduler refills, the algorithm stores the single
 * point in time at which the next permit becomes available ({@code theoreticalArrival}). Each
 * accepted request pushes that point forward by the emission interval, so refilling is implicit and
 * needs no timer, no thread and no memory growth. One {@link AtomicLong} plus one compare-and-set
 * per attempt is the entire synchronisation cost, which is why the limiter is lock-free.
 *
 * <p>The two limiter classes differ only in their burst tolerance:
 *
 * <ul>
 *   <li>token bucket: tolerance of {@code burstPermits * interval}, so a full bucket may be spent
 *       at once;</li>
 *   <li>leaky bucket: tolerance of one interval, so permits are released at a smooth, even pace.</li>
 * </ul>
 *
 * <p>Time is read from {@link System#nanoTime()}, so the limiter is immune to wall-clock changes,
 * never allocates on the fast path and adds no dependency.
 */
final class Gcra {

    /** Nanoseconds between two permits. */
    private final long emissionNanos;

    /** How far ahead of the clock the schedule may run before requests are rejected. */
    private final long toleranceNanos;

    /** Permits a single request may ask for, and the largest burst the schedule allows. */
    private final int burstPermits;

    /** Time at which the next permit becomes available; a single CAS-protected value. */
    private final AtomicLong theoreticalArrivalNanos;

    Gcra(int permitsPerWindow, Duration window, int burstPermits) {
        Checks.positive(permitsPerWindow, "permitsPerWindow");
        Checks.positive(burstPermits, "burstPermits");
        long windowNanos = Checks.nanos(window, "window");
        this.emissionNanos = Math.max(1L, windowNanos / permitsPerWindow);
        this.burstPermits = burstPermits;
        this.toleranceNanos = Checks.multiply(emissionNanos, burstPermits, "burst tolerance");
        this.theoreticalArrivalNanos = new AtomicLong(System.nanoTime());
    }

    int burstPermits() {
        return burstPermits;
    }

    int availablePermits() {
        long ahead = theoreticalArrivalNanos.get() - System.nanoTime();
        if (ahead <= 0) {
            // The schedule is behind the clock: the whole burst budget is free.
            return burstPermits;
        }
        if (ahead >= toleranceNanos) {
            return 0;
        }
        return (int) ((toleranceNanos - ahead) / emissionNanos);
    }

    /**
     * Grants {@code permits} if the schedule leaves room for them.
     *
     * <p>The check and the update happen inside a single compare-and-set, so concurrent callers
     * never see a half-updated schedule and the configured rate can never be exceeded.
     */
    boolean tryAcquire(int permits) {
        Checks.positive(permits, "permits");
        if (permits > burstPermits) {
            return false;
        }
        long step = permits * emissionNanos;
        long now = System.nanoTime();
        while (true) {
            long arrival = theoreticalArrivalNanos.get();
            long ahead = arrival - now;
            if (ahead <= 0) {
                // Idle bucket: restart the schedule from now. One full burst fits by definition.
                if (theoreticalArrivalNanos.compareAndSet(arrival, now + step)) {
                    return true;
                }
            } else if (ahead + step <= toleranceNanos) {
                if (theoreticalArrivalNanos.compareAndSet(arrival, arrival + step)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
