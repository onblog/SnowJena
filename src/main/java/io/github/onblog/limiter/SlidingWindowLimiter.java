package io.github.onblog.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Sliding window limiter: at most {@code maxPermits} per rolling window, with no boundary burst.
 *
 * <p>The window is divided into {@code slots} buckets (10 by default). Each bucket holds a counter
 * tagged with the window generation it belongs to; reading the limiter sums the buckets of the
 * current generation and ignores the stale ones. A request is therefore evaluated against the
 * recent past rather than against a counter that resets on a clock boundary, which is what makes
 * this limiter immune to the double-burst problem of {@link FixedWindowLimiter}: 100 permits at
 * 11:59:59 plus 100 permits at 12:00:01 never happen, because the rolling window still sees the
 * first batch.
 *
 * <p>Precision is one slot (a tenth of the window by default); memory is {@code slots} longs, fixed
 * and independent of traffic.
 *
 * <p>Under contention the limiter fails closed: a thread reserves its permits, re-reads the total
 * and rolls its reservation back if the window is already full. A request may therefore be rejected
 * during a microsecond of racing even though a permit was about to be released, but the limit itself
 * is never exceeded.
 *
 * <pre>{@code
 * // At most 1000 requests in any rolling 60 seconds.
 * RateLimiter limiter = new SlidingWindowLimiter("api", 1000, Duration.ofMinutes(1));
 * }</pre>
 *
 * <p>Instances are thread-safe and safe to share across threads.
 */
public final class SlidingWindowLimiter implements RateLimiter {

    /** Number of slots used when the caller does not choose one. */
    public static final int DEFAULT_SLOTS = 10;

    private static final long COUNTER_MASK = 0xFFFFFFFFL;

    private final String name;
    private final int maxPermits;
    private final int slotCount;
    private final long slotNanos;
    private final AtomicLongArray windows;

    /**
     * Creates a sliding window limiter with {@value #DEFAULT_SLOTS} slots.
     *
     * @param name       human-readable name used in logs and metrics
     * @param maxPermits permits allowed inside one rolling window
     * @param window     length of the rolling window
     * @throws IllegalArgumentException if any value is not positive
     */
    public SlidingWindowLimiter(String name, int maxPermits, Duration window) {
        this(name, maxPermits, window, DEFAULT_SLOTS);
    }

    /**
     * Creates a sliding window limiter.
     *
     * @param name       human-readable name used in logs and metrics
     * @param maxPermits permits allowed inside one rolling window
     * @param window     length of the rolling window
     * @param slotCount  buckets the window is divided into, a higher count means finer precision
     * @throws IllegalArgumentException if any value is not positive
     */
    public SlidingWindowLimiter(String name, int maxPermits, Duration window, int slotCount) {
        this.name = Checks.name(name);
        this.maxPermits = Checks.positive(maxPermits, "maxPermits");
        this.slotCount = Checks.positive(slotCount, "slotCount");
        long windowNanos = Checks.nanos(window, "window");
        this.slotNanos = Math.max(1L, windowNanos / this.slotCount);
        this.windows = new AtomicLongArray(this.slotCount);
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the permits allowed inside one rolling window */
    public int maxPermits() {
        return maxPermits;
    }

    @Override
    public int availablePermits() {
        int used = usedPermits(System.nanoTime() / slotNanos);
        int free = maxPermits - used;
        return free > 0 ? free : 0;
    }

    @Override
    public boolean tryAcquire(int permits) {
        Checks.positive(permits, "permits");
        if (permits > maxPermits) {
            return false;
        }
        long generation = System.nanoTime() / slotNanos;
        int index = (int) Math.floorMod(generation, (long) slotCount);
        if (usedPermits(generation) + permits > maxPermits) {
            return false;
        }
        reserve(index, generation, permits);
        if (usedPermits(generation) > maxPermits) {
            // Another thread raced us into an over-limit window: undo and fail closed.
            release(index, generation, permits);
            return false;
        }
        return true;
    }

    /** Sums the counters that belong to the live window, ignoring recycled slots. */
    private int usedPermits(long generation) {
        int total = 0;
        for (int i = 0; i < slotCount; i++) {
            long value = windows.get(i);
            long age = (generation - (value & COUNTER_MASK)) & COUNTER_MASK;
            if (age < slotCount) {
                total += (int) (value >>> 32);
            }
        }
        return total;
    }

    private void reserve(int index, long generation, int permits) {
        long generationBits = generation & COUNTER_MASK;
        long increment = ((long) permits) << 32;
        while (true) {
            long value = windows.get(index);
            long next = (value & COUNTER_MASK) == generationBits
                    ? value + increment                     // same slot, add to its counter
                    : increment | generationBits;           // recycle the stale slot
            if (windows.compareAndSet(index, value, next)) {
                return;
            }
        }
    }

    private void release(int index, long generation, int permits) {
        long generationBits = generation & COUNTER_MASK;
        long decrement = ((long) permits) << 32;
        while (true) {
            long value = windows.get(index);
            if ((value & COUNTER_MASK) != generationBits) {
                return; // The slot was recycled meanwhile, so its counter no longer counts.
            }
            if (windows.compareAndSet(index, value, value - decrement)) {
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "SlidingWindowLimiter{name='" + name + "', maxPermits=" + maxPermits
                + ", slots=" + slotCount + '}';
    }
}
