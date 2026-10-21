package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.onblog.limiter.TokenBucketLimiterTest.runConcurrently;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowLimiterTest {

    @Test
    void allowsUpToTheLimit() {
        FixedWindowLimiter limiter = new FixedWindowLimiter("t", 5, Duration.ofSeconds(10));
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire(), "permit " + i + " should be granted");
        }
        assertFalse(limiter.tryAcquire());
        assertEquals(0, limiter.availablePermits());
        assertEquals(5, limiter.maxPermits());
    }

    @Test
    void counterResetsInTheNextWindow() throws InterruptedException {
        FixedWindowLimiter limiter = new FixedWindowLimiter("t", 5, Duration.ofMillis(100));
        assertTrue(limiter.tryAcquire(5));
        assertFalse(limiter.tryAcquire());

        Thread.sleep(150);
        assertEquals(5, limiter.availablePermits(), "a new window starts with a full budget");
        assertTrue(limiter.tryAcquire(5));
    }

    @Test
    void concurrentCallersCannotExceedTheWindow() throws InterruptedException {
        FixedWindowLimiter limiter = new FixedWindowLimiter("t", 20, Duration.ofSeconds(10));
        AtomicInteger granted = new AtomicInteger();
        runConcurrently(16, 100, () -> {
            if (limiter.tryAcquire()) {
                granted.incrementAndGet();
            }
        });
        assertEquals(20, granted.get(), "exactly the window limit must be granted");
    }

    @Test
    void rejectsRequestsLargerThanTheWindow() {
        FixedWindowLimiter limiter = new FixedWindowLimiter("t", 10, Duration.ofSeconds(10));
        assertFalse(limiter.tryAcquire(11));
        assertEquals(10, limiter.availablePermits());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new FixedWindowLimiter("", 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new FixedWindowLimiter("t", 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new FixedWindowLimiter("t", 1, Duration.ZERO));

        FixedWindowLimiter limiter = new FixedWindowLimiter("t", 1, Duration.ofSeconds(1));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(0));
    }
}
