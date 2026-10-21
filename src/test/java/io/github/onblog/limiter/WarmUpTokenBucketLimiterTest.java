package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarmUpTokenBucketLimiterTest {

    @Test
    void startsAtOneThirdOfTheRateAndBurst() {
        // 300 per second once warm, so 10ms per permit when cold; a 30 permit burst becomes 10.
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 30, 300, Duration.ofSeconds(1), Duration.ofMillis(200));
        int available = limiter.availablePermits();
        assertTrue(available >= 8 && available <= 11,
                "a warm-up limiter should start with about a third of its burst, got " + available);
        assertEquals(3, limiter.coldFactor());
        assertEquals(30, limiter.capacity());
    }

    @Test
    void rejectsMoreThanTheColdBurst() {
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 30, 300, Duration.ofSeconds(1), Duration.ofMillis(200));
        assertFalse(limiter.tryAcquire(11), "the burst is reduced while cold");
        assertTrue(limiter.tryAcquire(10), "about a third of the burst is still grantable at once");
    }

    @Test
    void reachesFullRateAfterTheWarmUpPeriod() throws InterruptedException {
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 30, 300, Duration.ofSeconds(1), Duration.ofMillis(150));
        Thread.sleep(220);
        assertEquals(1d, limiter.warmUpProgress(), 0.001);
        assertEquals(30, limiter.availablePermits(), "the full burst is available once warm");
        assertTrue(limiter.tryAcquire(30));
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void pacesSlowerWhileCold() throws InterruptedException {
        // A capacity of one makes the burst irrelevant, so this measures the rate alone:
        // 100 per second once warm => 10ms per permit; cold => about 30ms per permit.
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 1, 100, Duration.ofSeconds(1), Duration.ofMillis(400));
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire(), "a second permit is not available immediately when cold");

        Thread.sleep(45);
        assertTrue(limiter.tryAcquire(), "one cold interval has passed");
    }

    @Test
    void coldBurstIsCapacityOverColdFactor() {
        // The burst budget is a fixed number of intervals, so a colder (longer) interval means fewer
        // permits at once: 30 permits warm, about 10 while cold.
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 30, 300, Duration.ofSeconds(1), Duration.ofMillis(500));
        int granted = 0;
        while (limiter.tryAcquire(10)) {
            granted += 10;
            if (granted >= 30) {
                break;
            }
        }
        assertEquals(10, granted, "while cold exactly one cold factor worth of the burst fits");
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void progressRampsFromZeroToOne() throws InterruptedException {
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 10, 100, Duration.ofSeconds(1), Duration.ofMillis(300));
        assertTrue(limiter.warmUpProgress() < 0.2, "the ramp has just started");

        Thread.sleep(150);
        double halfway = limiter.warmUpProgress();
        assertTrue(halfway > 0.3 && halfway < 0.8, "the ramp is about halfway, got " + halfway);

        Thread.sleep(200);
        assertEquals(1d, limiter.warmUpProgress(), 0.001);
    }

    @Test
    void availablePermitsGrowWithTheRamp() throws InterruptedException {
        // Window long enough that nothing refills by pacing, so only the ramp changes the burst.
        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 30, 30, Duration.ofSeconds(1), Duration.ofMillis(300));
        int cold = limiter.availablePermits();
        Thread.sleep(160);
        int warming = limiter.availablePermits();
        Thread.sleep(200);
        int warm = limiter.availablePermits();

        assertTrue(cold < warming, "available permits grow during warm-up: " + cold + " -> " + warming);
        assertTrue(warming <= warm, "and keep growing: " + warming + " -> " + warm);
        assertEquals(30, warm);
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "", 1, 1, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "t", 0, 1, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "t", 1, 0, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "t", 1, 1, Duration.ZERO, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "t", 1, 1, Duration.ofSeconds(1), Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new WarmUpTokenBucketLimiter(
                "t", 1, 1, Duration.ofSeconds(1), null));

        WarmUpTokenBucketLimiter limiter =
                new WarmUpTokenBucketLimiter("t", 4, 4, Duration.ofSeconds(1), Duration.ofMillis(100));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(0));
    }
}
