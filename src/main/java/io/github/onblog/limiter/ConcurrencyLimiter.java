package io.github.onblog.limiter;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency limiter: caps how many requests may be in flight at the same time.
 *
 * <p>This measures a different dimension than the rate limiters. A token bucket answers "how many
 * requests per second", which does nothing when the problem is a slow downstream: at 10 requests per
 * second and a 3 second response time there are already 30 requests in flight, and a thread pool,
 * connection pool or file handle limit gives out long before the rate does. A concurrency limiter
 * answers "how many at once", which is the bound that actually protects them. It is the same idea as
 * Nginx's {@code limit_conn} or a Resilience4j bulkhead.
 *
 * <p>A permit is a slot, not a token: it is not consumed by the clock, it is given back. Every
 * successful {@link #tryAcquire()} must be paired with a {@link #release()}, which is why the
 * try-with-resources form exists and should be the default choice:
 *
 * <pre>{@code
 * ConcurrencyLimiter limiter = new ConcurrencyLimiter("db-pool", 20);
 *
 * try (ConcurrencyLimiter.Permit permit = limiter.tryEnter()) {
 *     if (permit == null) {
 *         response.setStatus(429);        // all slots busy
 *         return;
 *     }
 *     queryDatabase();
 * }                                       // released here, even when queryDatabase() throws
 * }</pre>
 *
 * <p>For a block of work with no early return, {@link #call(Callable)} and {@link #execute(Runnable)}
 * acquire and release around the task:
 *
 * <pre>{@code
 * List<Row> rows = limiter.call(() -> queryDatabase(sql));   // IllegalStateException when full
 * }</pre>
 *
 * <p>Instances are thread-safe and safe to share across threads. Nothing is scheduled and nothing is
 * allocated per call.
 */
public final class ConcurrencyLimiter implements RateLimiter {

    private final String name;
    private final int maxConcurrency;
    private final AtomicInteger inFlight = new AtomicInteger();

    /**
     * Creates a concurrency limiter.
     *
     * @param name           human-readable name used in logs and metrics
     * @param maxConcurrency maximum number of permits held at the same time
     * @throws IllegalArgumentException if the name is empty or {@code maxConcurrency} is not positive
     */
    public ConcurrencyLimiter(String name, int maxConcurrency) {
        this.name = Checks.name(name);
        this.maxConcurrency = Checks.positive(maxConcurrency, "maxConcurrency");
    }

    /** A held slot. Closing it returns the slot, and closing twice is a no-op. */
    public interface Permit extends AutoCloseable {

        /** Returns the slot. Idempotent: only the first call has an effect. */
        @Override
        void close();
    }

    @Override
    public String name() {
        return name;
    }

    /** @return the maximum number of permits that may be held at once */
    public int maxConcurrency() {
        return maxConcurrency;
    }

    /** @return how many permits are held right now */
    public int inFlight() {
        return inFlight.get();
    }

    @Override
    public int availablePermits() {
        int free = maxConcurrency - inFlight.get();
        return free > 0 ? free : 0;
    }

    @Override
    public boolean tryAcquire(int permits) {
        Checks.positive(permits, "permits");
        if (permits > maxConcurrency) {
            return false;
        }
        while (true) {
            int current = inFlight.get();
            if (current + permits > maxConcurrency) {
                return false;
            }
            if (inFlight.compareAndSet(current, current + permits)) {
                return true;
            }
        }
    }

    /**
     * Returns {@code permits} slots to the limiter.
     *
     * @param permits number of slots to return, must be positive
     * @throws IllegalStateException    if more slots are returned than are held
     * @throws IllegalArgumentException if {@code permits} is not positive
     */
    public void release(int permits) {
        Checks.positive(permits, "permits");
        while (true) {
            int current = inFlight.get();
            if (current < permits) {
                throw new IllegalStateException(
                        "released " + permits + " permits on limiter '" + name + "' but only " + current + " are held");
            }
            if (inFlight.compareAndSet(current, current - permits)) {
                return;
            }
        }
    }

    /** Returns one slot to the limiter. */
    public void release() {
        release(1);
    }

    /**
     * Enters one slot, for use in a try-with-resources block.
     *
     * @return a permit that must be closed, or {@code null} when every slot is busy
     */
    public Permit tryEnter() {
        return tryEnter(1);
    }

    /**
     * Enters {@code permits} slots, for use in a try-with-resources block.
     *
     * @param permits number of slots to enter
     * @return a permit that must be closed, or {@code null} when not enough slots are free
     * @throws IllegalArgumentException if {@code permits} is not positive
     */
    public Permit tryEnter(int permits) {
        if (!tryAcquire(permits)) {
            return null;
        }
        return new Permit() {
            private boolean closed;

            @Override
            public synchronized void close() {
                if (!closed) {
                    closed = true;
                    release(permits);
                }
            }
        };
    }

    /**
     * Runs the task while holding one slot, releasing it afterwards even if the task throws.
     *
     * @param task the work to run
     * @throws IllegalStateException if every slot is busy, or if the task throws it
     */
    public void execute(Runnable task) {
        try (Permit permit = tryEnter()) {
            if (permit == null) {
                throw new IllegalStateException("concurrency limit of " + maxConcurrency + " reached on limiter '" + name + "'");
            }
            task.run();
        }
    }

    /**
     * Runs the task while holding one slot, releasing it afterwards even if the task throws.
     *
     * @param task the work to run
     * @param <T>  result type
     * @return whatever the task returns
     * @throws IllegalStateException if every slot is busy, or if the task throws
     */
    public <T> T call(Callable<T> task) {
        try (Permit permit = tryEnter()) {
            if (permit == null) {
                throw new IllegalStateException("concurrency limit of " + maxConcurrency + " reached on limiter '" + name + "'");
            }
            try {
                return task.call();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("task failed on limiter '" + name + "'", e);
            }
        }
    }

    @Override
    public String toString() {
        return "ConcurrencyLimiter{name='" + name + "', maxConcurrency=" + maxConcurrency
                + ", inFlight=" + inFlight.get() + '}';
    }
}
