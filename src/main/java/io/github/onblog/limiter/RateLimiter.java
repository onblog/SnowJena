package io.github.onblog.limiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A lock-free, thread-safe rate limiter.
 *
 * <p>All implementations are non-blocking on the fast path: a permit check costs a handful of
 * atomic reads plus, at most, one compare-and-set. No implementation starts a background thread,
 * holds a mutex, or allocates per call.
 *
 * <p>A limiter is created for a rate, for example "600 permits per minute", and is safe to share
 * between threads. How the rate is enforced depends on the implementation:
 *
 * <ul>
 *   <li>{@link TokenBucketLimiter} — average rate plus a configurable burst.</li>
 *   <li>{@link LeakyBucketLimiter} — strict smooth pacing, no burst.</li>
 *   <li>{@link SlidingWindowLimiter} — at most N permits per rolling window.</li>
 *   <li>{@link FixedWindowLimiter} — at most N permits per aligned window (cheapest).</li>
 * </ul>
 *
 * <p>Rejected requests are the caller's business: a {@code false} return means "do not perform the
 * action", not an error. Nothing is logged or thrown for a rejection.
 */
public interface RateLimiter {

    /**
     * Human-readable name of this limiter, for logs and metrics.
     *
     * @return the name given at construction time
     */
    String name();

    /**
     * Permits that could be acquired right now without waiting.
     *
     * <p>This is a snapshot: another thread may consume the permits before the caller acts on the
     * value, so it is meant for reporting and adaptive logic, never for enforcing a limit.
     *
     * @return a value between {@code 0} and the configured maximum
     */
    int availablePermits();

    /**
     * Acquires a single permit.
     *
     * @return {@code true} if the request is allowed, {@code false} if it must be rejected
     */
    default boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Acquires {@code permits} permits at once, atomically: either all of them are granted or none.
     *
     * @param permits number of permits, must be positive
     * @return {@code true} if the request is allowed, {@code false} if it must be rejected
     * @throws IllegalArgumentException if {@code permits} is not positive
     */
    boolean tryAcquire(int permits);

    /**
     * Blocks until {@code permits} permits are granted or the timeout elapses.
     *
     * <p>The waiting thread is parked, not spinning, and is woken every millisecond to retry. Use
     * this when the caller prefers back pressure to rejection; use {@link #tryAcquire(int)} inside
     * a request path where waiting is not allowed.
     *
     * @param permits   number of permits, must be positive
     * @param timeout   maximum time to wait, negative is treated as zero
     * @param unit      unit of {@code timeout}
     * @return {@code true} if the permits were granted in time, {@code false} otherwise
     * @throws IllegalArgumentException if {@code permits} is not positive
     * @throws InterruptedException     if the thread is interrupted while waiting
     */
    default boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException {
        if (tryAcquire(permits)) {
            return true;
        }
        long pollNanos = TimeUnit.MILLISECONDS.toNanos(1);
        long deadline = System.nanoTime() + unit.toNanos(Math.max(0L, timeout));
        while (true) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return false;
            }
            LockSupport.parkNanos(Math.min(remaining, pollNanos));
            if (Thread.interrupted()) {
                throw new InterruptedException("interrupted while waiting for a permit on limiter '" + name() + "'");
            }
            if (tryAcquire(permits)) {
                return true;
            }
        }
    }
}
