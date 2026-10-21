package io.github.onblog.limiter;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.onblog.limiter.TokenBucketLimiterTest.runConcurrently;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyLimiterTest {

    @Test
    void capsThePermitsHeldAtOnce() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 2);
        assertEquals(2, limiter.maxConcurrency());
        assertEquals(2, limiter.availablePermits());

        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire(), "both slots are taken");
        assertEquals(0, limiter.availablePermits());
        assertEquals(2, limiter.inFlight());
    }

    @Test
    void releasingFreesASlot() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());

        limiter.release();
        assertTrue(limiter.tryAcquire(), "the freed slot can be used again");
        assertEquals(1, limiter.inFlight());
    }

    @Test
    void slotsAreUnlimitedByTime() throws InterruptedException {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 2);
        assertTrue(limiter.tryAcquire(2));
        Thread.sleep(80);
        // Unlike a token bucket, a slot never comes back on its own.
        assertEquals(2, limiter.inFlight());
        assertEquals(0, limiter.availablePermits());
    }

    @Test
    void permitReleasesOnClose() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 3);
        try (ConcurrencyLimiter.Permit permit = limiter.tryEnter()) {
            assertNotNull(permit);
            assertEquals(2, limiter.availablePermits());
        }
        assertEquals(3, limiter.availablePermits());
    }

    @Test
    void permitCloseIsIdempotent() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        ConcurrencyLimiter.Permit permit = limiter.tryEnter();
        assertNotNull(permit);
        permit.close();
        permit.close();
        assertEquals(1, limiter.availablePermits(), "closing twice must not free a second slot");
    }

    @Test
    void tryEnterReturnsNullWhenFull() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        ConcurrencyLimiter.Permit first = limiter.tryEnter();
        assertNotNull(first);
        assertNull(limiter.tryEnter(), "no slot is left, so no permit is handed out");
        first.close();
        assertNotNull(limiter.tryEnter());
    }

    @Test
    void callReleasesTheSlotWhenTheTaskThrows() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        RuntimeException boom = new RuntimeException("boom");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> limiter.call(() -> {
            throw boom;
        }));
        assertTrue(thrown == boom, "the original exception must propagate unchanged");
        assertEquals(0, limiter.inFlight(), "the slot must be released");
        assertTrue(limiter.tryAcquire());
    }

    @Test
    void callReturnsTheTaskResult() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        assertEquals("done", limiter.call(() -> "done"));
        assertEquals(0, limiter.inFlight());
        limiter.execute(() -> {
        });
        assertEquals(0, limiter.inFlight());
    }

    @Test
    void callRejectsWhenEverySlotIsBusy() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        assertTrue(limiter.tryAcquire());
        assertThrows(IllegalStateException.class, () -> limiter.call(() -> "never"));
        assertEquals(1, limiter.inFlight(), "a rejected call must not change the count");
    }

    @Test
    void concurrentCallersCannotExceedTheLimit() throws InterruptedException {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 4);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        runConcurrently(16, 300, () -> {
            try (ConcurrencyLimiter.Permit permit = limiter.tryEnter()) {
                if (permit == null) {
                    return;
                }
                peak.accumulateAndGet(running.incrementAndGet(), Math::max);
                Thread.yield();
                running.decrementAndGet();
            }
        });
        assertTrue(peak.get() <= 4, "at most 4 permits may be held at once, peak was " + peak.get());
        assertEquals(0, limiter.inFlight(), "every permit must be returned");
    }

    @Test
    void blockingAcquireWaitsForARelease() throws InterruptedException {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 1);
        assertTrue(limiter.tryAcquire());

        Thread releaser = new Thread(() -> {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            limiter.release();
        });
        releaser.start();

        assertTrue(limiter.tryAcquire(1, 2, TimeUnit.SECONDS), "the wait should end when the slot is released");
        releaser.join();
    }

    @Test
    void releasingMoreThanHeldIsRejected() {
        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 2);
        assertTrue(limiter.tryAcquire());
        assertThrows(IllegalStateException.class, () -> limiter.release(3));
        limiter.release();
        assertThrows(IllegalStateException.class, limiter::release);
        assertEquals(0, limiter.inFlight());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new ConcurrencyLimiter("", 1));
        assertThrows(IllegalArgumentException.class, () -> new ConcurrencyLimiter(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ConcurrencyLimiter("db", 0));

        ConcurrencyLimiter limiter = new ConcurrencyLimiter("db", 2);
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(0));
        assertThrows(IllegalArgumentException.class, () -> limiter.release(0));
        assertFalse(limiter.tryAcquire(3), "asking for more slots than the limit is a rejection, not an error");
    }
}
