package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers the blocking {@code tryAcquire(permits, timeout, unit)} default method. */
class BlockingAcquireTest {

    @Test
    void returnsImmediatelyWhenAPermitIsFree() throws InterruptedException {
        RateLimiter limiter = RateLimiters.tokenBucket("t", 10);
        assertTrue(limiter.tryAcquire(1, 0, TimeUnit.MILLISECONDS));
    }

    @Test
    void waitsUntilAPermitIsReleased() throws InterruptedException {
        // 100 permits per second: the next permit arrives within about 10ms.
        RateLimiter limiter = RateLimiters.leakyBucket("t", 100);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS), "the permit should arrive within 500ms");
    }

    @Test
    void givesUpAfterTheTimeout() throws InterruptedException {
        RateLimiter limiter = RateLimiters.tokenBucket("t", 1, 1, Duration.ofSeconds(60));
        assertTrue(limiter.tryAcquire());

        long start = System.nanoTime();
        assertFalse(limiter.tryAcquire(1, 40, TimeUnit.MILLISECONDS));
        long waitedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(waitedMillis >= 35, "it should have waited for the timeout, waited " + waitedMillis + "ms");
    }

    @Test
    void negativeTimeoutFailsFast() throws InterruptedException {
        RateLimiter limiter = RateLimiters.tokenBucket("t", 1, 1, Duration.ofSeconds(60));
        assertTrue(limiter.tryAcquire());

        long start = System.nanoTime();
        assertFalse(limiter.tryAcquire(1, -5, TimeUnit.SECONDS));
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 200);
    }

    @Test
    void interruptedWaitThrowsAndClearsTheFlag() {
        RateLimiter limiter = RateLimiters.tokenBucket("t", 1, 1, Duration.ofSeconds(60));
        assertTrue(limiter.tryAcquire());

        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, () -> limiter.tryAcquire(1, 5, TimeUnit.SECONDS));
        assertFalse(Thread.interrupted(), "the interrupt flag must be consumed");
    }
}
