package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.onblog.limiter.TokenBucketLimiterTest.runConcurrently;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowLimiterTest {

    @Test
    void allowsUpToTheLimitAndNoMore() {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 10, Duration.ofSeconds(10));
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire(), "permit " + i + " should be granted");
        }
        assertFalse(limiter.tryAcquire());
        assertEquals(0, limiter.availablePermits());
        assertEquals(10, limiter.maxPermits());
    }

    @Test
    void windowKeepsRollingForward() throws InterruptedException {
        // 200ms window in 10 slots => 20ms per slot.
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 10, Duration.ofMillis(200));
        assertTrue(limiter.tryAcquire(5));
        Thread.sleep(100);
        assertTrue(limiter.tryAcquire(5), "the oldest permits are still inside the window");
        assertEquals(0, limiter.availablePermits());

        Thread.sleep(130);
        int halfWay = limiter.availablePermits();
        assertTrue(halfWay >= 4 && halfWay <= 6,
                "the first batch should have left the window, got " + halfWay);

        Thread.sleep(130);
        assertEquals(10, limiter.availablePermits(), "the whole window has expired");
    }

    @Test
    void permitsAreCountedUntilTheirSlotLeavesTheWindow() throws InterruptedException {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 10, Duration.ofMillis(200));
        assertTrue(limiter.tryAcquire(10));

        Thread.sleep(120);
        assertEquals(0, limiter.availablePermits(), "a 120ms old batch is still inside a 200ms window");

        Thread.sleep(160);
        assertEquals(10, limiter.availablePermits());
    }

    @Test
    void rejectsRequestsLargerThanTheWindow() {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 10, Duration.ofSeconds(10));
        assertFalse(limiter.tryAcquire(11));
        assertEquals(10, limiter.availablePermits());
    }

    @Test
    void concurrentCallersCannotExceedTheWindow() throws InterruptedException {
        // A 10s window cannot roll during the test, so the outcome is deterministic.
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 20, Duration.ofSeconds(10));
        AtomicInteger granted = new AtomicInteger();
        runConcurrently(16, 100, () -> {
            if (limiter.tryAcquire()) {
                granted.incrementAndGet();
            }
        });
        assertEquals(20, granted.get(), "exactly the window limit must be granted");
        assertEquals(0, limiter.availablePermits(), "reservations must be rolled back, not leaked");
    }

    @Test
    void concurrentMultiPermitRequestsStayWithinTheWindow() throws InterruptedException {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 30, Duration.ofSeconds(10));
        AtomicInteger permits = new AtomicInteger();
        runConcurrently(12, 40, () -> {
            if (limiter.tryAcquire(3)) {
                permits.addAndGet(3);
            }
        });
        assertTrue(permits.get() <= 30, "granted permits must stay within the window, got " + permits.get());
        assertEquals(30, permits.get(), "every permit of the window should be handed out");
    }

    @Test
    void customSlotCountIsSupported() {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter("t", 5, Duration.ofSeconds(1), 1);
        assertTrue(limiter.tryAcquire(5));
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowLimiter("", 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowLimiter("t", 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowLimiter("t", 1, Duration.ofSeconds(1), 0));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowLimiter("t", 1, Duration.ZERO));
    }
}
