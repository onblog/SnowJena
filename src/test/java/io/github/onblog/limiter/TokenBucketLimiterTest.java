package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketLimiterTest {

    @Test
    void startsFull() {
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 10, 10, Duration.ofSeconds(1));
        assertEquals(10, limiter.availablePermits());
        assertEquals(10, limiter.capacity());
        assertEquals("t", limiter.name());
    }

    @Test
    void allowsExactlyOneBurstThenRejects() {
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 5, 1, Duration.ofSeconds(1));
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire(), "burst permit " + i + " should be granted");
        }
        assertFalse(limiter.tryAcquire(), "the burst is used up");
        assertEquals(0, limiter.availablePermits());
    }

    @Test
    void refillsOverTime() throws InterruptedException {
        // 100 permits per second => one permit every 10ms, no burst beyond the capacity.
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 3, 100, Duration.ofSeconds(1));
        assertTrue(limiter.tryAcquire(3));
        assertEquals(0, limiter.availablePermits());

        Thread.sleep(55);
        int available = limiter.availablePermits();
        assertTrue(available >= 3 && available <= 5, "about 5 permits should have refilled, got " + available);
        assertTrue(limiter.tryAcquire(3), "the refilled permits should be usable");
    }

    @Test
    void neverRefillsBeyondCapacity() throws InterruptedException {
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 4, 100_000, Duration.ofSeconds(1));
        assertTrue(limiter.tryAcquire(4));
        Thread.sleep(20);
        assertEquals(4, limiter.availablePermits(), "the bucket must not overflow its capacity");
    }

    @Test
    void rejectsRequestsLargerThanTheBurst() {
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 10, 10, Duration.ofSeconds(1));
        assertFalse(limiter.tryAcquire(11));
        assertEquals(10, limiter.availablePermits(), "a rejected request must not consume permits");
    }

    @Test
    void concurrentCallersCannotExceedTheBurst() throws InterruptedException {
        // A refill of 1 permit per second makes the test deterministic: no permit comes back while it runs.
        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 100, 1, Duration.ofSeconds(1));
        AtomicInteger granted = new AtomicInteger();
        runConcurrently(8, 200, () -> {
            if (limiter.tryAcquire()) {
                granted.incrementAndGet();
            }
        });
        assertEquals(100, granted.get(), "exactly the burst capacity must be granted");
        assertEquals(0, limiter.availablePermits());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter("", 1, 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter(null, 1, 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter("t", 0, 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter("t", 1, 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter("t", 1, 1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new TokenBucketLimiter("t", 1, 1, null));

        TokenBucketLimiter limiter = new TokenBucketLimiter("t", 1, 1, Duration.ofSeconds(1));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(0));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(-3));
    }

    /** Starts {@code threads} threads that each run {@code action} {@code perThread} times. */
    static void runConcurrently(int threads, int perThread, Runnable action) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        action.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers must finish in time");
        pool.shutdownNow();
    }
}
