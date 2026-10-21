package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitersTest {

    @Test
    void tokenBucketPerSecondBurstsUpToTheRate() {
        TokenBucketLimiter limiter = RateLimiters.tokenBucket("api", 5);
        assertEquals("api", limiter.name());
        assertEquals(5, limiter.capacity());
        assertTrue(limiter.tryAcquire(5));
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void tokenBucketWithExplicitBurst() {
        TokenBucketLimiter limiter = RateLimiters.tokenBucket("api", 10, 5, Duration.ofSeconds(1));
        assertEquals(10, limiter.capacity());
        assertTrue(limiter.tryAcquire(10), "the whole burst fits");
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void leakyBucketIsSinglePermit() {
        LeakyBucketLimiter limiter = RateLimiters.leakyBucket("upstream", 50);
        assertEquals("upstream", limiter.name());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void slidingAndFixedWindowsExposeTheirLimit() {
        SlidingWindowLimiter sliding = RateLimiters.slidingWindow("login", 5, Duration.ofMinutes(1));
        assertEquals(5, sliding.maxPermits());
        FixedWindowLimiter fixed = RateLimiters.fixedWindow("reports", 7, Duration.ofHours(1));
        assertEquals(7, fixed.maxPermits());
        assertEquals("reports", fixed.name());
    }

    @Test
    void everyImplementationHonoursTheSharedContract() {
        RateLimiter[] limiters = {
                RateLimiters.tokenBucket("a", 2),
                RateLimiters.leakyBucket("b", 2),
                RateLimiters.slidingWindow("c", 2, Duration.ofSeconds(10)),
                RateLimiters.fixedWindow("d", 2, Duration.ofSeconds(10)),
        };
        for (RateLimiter limiter : limiters) {
            assertFalse(limiter.name().isEmpty(), "every limiter is named");
            assertTrue(limiter.availablePermits() >= 1, limiter.name() + " starts with a free permit");
            assertTrue(limiter.tryAcquire(), limiter.name() + " must grant a free permit");
            assertTrue(limiter.availablePermits() >= 0, limiter.name() + " never reports a negative balance");
        }
    }

    @Test
    void namesAreRequired() {
        assertThrows(IllegalArgumentException.class, () -> RateLimiters.tokenBucket("  ", 1));
        assertThrows(IllegalArgumentException.class, () -> RateLimiters.leakyBucket(null, 1));
        assertThrows(IllegalArgumentException.class, () -> RateLimiters.slidingWindow("", 1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> RateLimiters.fixedWindow(null, 1, Duration.ofSeconds(1)));
    }
}
