package io.github.onblog.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed window limiter: at most {@code maxPermits} per aligned window, the cheapest option.
 *
 * <p>The whole state is one {@link AtomicLong} holding the window index in its high bits and the
 * used-permit count in its low bits, so a permit check is a single atomic read and, at most, one
 * compare-and-set. That makes this the limiter to pick for very high request rates with modest
 * accuracy requirements.
 *
 * <p>Its known weakness is the boundary burst: a client may spend the full budget at the very end
 * of one window and the full budget again at the very start of the next, briefly reaching twice the
 * configured rate. Pick {@link SlidingWindowLimiter} when that pattern is unacceptable.
 *
 * <pre>{@code
 * // Reset on the minute: at most 100 permits per aligned minute.
 * RateLimiter limiter = new FixedWindowLimiter("reports", 100, Duration.ofMinutes(1));
 * }</pre>
 *
 * <p>Instances are thread-safe and safe to share across threads.
 */
public final class FixedWindowLimiter implements RateLimiter {

    private static final long COUNTER_MASK = 0xFFFFFFFFL;

    private final String name;
    private final int maxPermits;
    private final long windowNanos;

    /** High 32 bits: window index. Low 32 bits: permits already used in that window. */
    private final AtomicLong state = new AtomicLong();

    /**
     * Creates a fixed window limiter.
     *
     * @param name       human-readable name used in logs and metrics
     * @param maxPermits permits allowed inside one window
     * @param window     length of the window
     * @throws IllegalArgumentException if any value is not positive
     */
    public FixedWindowLimiter(String name, int maxPermits, Duration window) {
        this.name = Checks.name(name);
        this.maxPermits = Checks.positive(maxPermits, "maxPermits");
        this.windowNanos = Math.max(1L, Checks.nanos(window, "window"));
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the permits allowed inside one window */
    public int maxPermits() {
        return maxPermits;
    }

    @Override
    public int availablePermits() {
        long value = state.get();
        if ((value >>> 32) != currentWindow()) {
            return maxPermits;
        }
        int free = maxPermits - (int) (value & COUNTER_MASK);
        return free > 0 ? free : 0;
    }

    @Override
    public boolean tryAcquire(int permits) {
        Checks.positive(permits, "permits");
        if (permits > maxPermits) {
            return false;
        }
        long window = currentWindow();
        while (true) {
            long value = state.get();
            if ((value >>> 32) != window) {
                // First request of a new window: replace the whole state.
                if (state.compareAndSet(value, (window << 32) | permits)) {
                    return true;
                }
                continue;
            }
            if ((int) (value & COUNTER_MASK) + permits > maxPermits) {
                return false;
            }
            if (state.compareAndSet(value, value + permits)) {
                return true;
            }
        }
    }

    private long currentWindow() {
        return (System.nanoTime() / windowNanos) & COUNTER_MASK;
    }

    @Override
    public String toString() {
        return "FixedWindowLimiter{name='" + name + "', maxPermits=" + maxPermits + '}';
    }
}
