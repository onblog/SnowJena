package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.onblog.limiter.TokenBucketLimiterTest.runConcurrently;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeakyBucketLimiterTest {

    @Test
    void holdsASinglePermit() {
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("t", 100, Duration.ofSeconds(1));
        assertEquals(1, limiter.availablePermits());
        assertEquals(100, limiter.permitsPerWindow());
    }

    @Test
    void neverAccumulatesABurst() throws InterruptedException {
        // 100 per second means one permit every 10ms; an idle limiter still releases one at a time.
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("t", 100, Duration.ofSeconds(1));
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire(), "the second permit is not available yet");
        assertFalse(limiter.tryAcquire(), "still not available");

        Thread.sleep(15);
        assertTrue(limiter.tryAcquire(), "the permit becomes available after one interval");
    }

    @Test
    void pacesEvenAfterALongIdlePeriod() throws InterruptedException {
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("t", 1000, Duration.ofSeconds(1));
        Thread.sleep(30);
        // A token bucket would now allow 30 queued permits; a leaky bucket allows exactly one.
        assertEquals(1, limiter.availablePermits());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void rejectsAnyRequestForMoreThanOnePermit() {
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("t", 100, Duration.ofSeconds(1));
        assertFalse(limiter.tryAcquire(2));
        assertEquals(1, limiter.availablePermits());
    }

    @Test
    void concurrentCallersArePaced() throws InterruptedException {
        // 10 permits per second: within the few milliseconds of the test only one may pass.
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("t", 10, Duration.ofSeconds(1));
        AtomicInteger granted = new AtomicInteger();
        runConcurrently(8, 50, () -> {
            if (limiter.tryAcquire()) {
                granted.incrementAndGet();
            }
        });
        assertTrue(granted.get() <= 2, "at most one permit per 100ms may pass, got " + granted.get());
        assertTrue(granted.get() >= 1, "the first permit must pass");
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new LeakyBucketLimiter("", 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new LeakyBucketLimiter("t", 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new LeakyBucketLimiter("t", 1, Duration.ZERO));
    }
}
