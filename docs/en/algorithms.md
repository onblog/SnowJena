# Algorithms

Four limiters, one interface. They exist because "limit the rate" hides four different questions:
does a burst fit through, must the pace be smooth, and how is the window boundary defined?

| Limiter | Rate model | Burst | State | Cost per request |
| --- | --- | --- | --- | --- |
| `TokenBucketLimiter` | average rate, burst up to capacity | up to `capacity` | one `long` | 1 read + 1 CAS |
| `LeakyBucketLimiter` | strictly even pacing | 1 | one `long` | 1 read + 1 CAS |
| `SlidingWindowLimiter` | N per rolling window | up to N | `slots` longs | `slots` reads + 1 CAS (+ 1 read, rare rollback) |
| `FixedWindowLimiter` | N per aligned window | up to 2N at the boundary | one `long` | 1 read + 1 CAS |

## Token bucket (virtual scheduling / GCRA)

A token bucket has two parameters: how many permits are added per interval (the rate) and how many the
bucket can hold (the capacity, i.e. the largest burst). Permits accumulate while the limiter is idle
and are spent by requests.

Instead of a token counter plus a refill thread, `TokenBucketLimiter` stores a single number: the
moment at which the next permit becomes available, called the theoretical arrival time. Every accepted
request pushes that moment forward by one emission interval. Refilling then happens implicitly,
because time itself is the refill.

```
emission interval T = window / permitsPerWindow
tolerance τ         = capacity * T

accept(permits) when   (max(arrival, now) - now) + permits * T  <=  τ
then                   arrival = max(arrival, now) + permits * T
```

For a rate of 100/s with a capacity of 3, one permit is released every 10ms and at most three may be
spent at once:

```
time  0ms   10ms   20ms   30ms   40ms
      ●───●──────●──────●──────●───▶  permits become available (T = 10ms)
      ▓▓▓                              burst of 3 right now, then 1 per 10ms
```

The bucket never overflows: while idle, the schedule stays behind the clock and `availablePermits()`
reports the capacity, not more.

## Leaky bucket

A leaky bucket behaves like a queue that drains at a constant speed. It does not matter how long the
limiter has been idle: after one permit is granted, the next one is exactly one emission interval away.

Mathematically this is the same virtual scheduling as the token bucket with a tolerance of a single
interval, so both classes share the GCRA core and differ only in their burst tolerance. That is not a
re-labelling — it is the reason the two classes produce genuinely different admission decisions:

```java
TokenBucketLimiter bucket = RateLimiters.tokenBucket("t", 3, 100, Duration.ofSeconds(1));
Thread.sleep(50);
bucket.availablePermits();   // 3 — the burst accumulated

LeakyBucketLimiter leak = RateLimiters.leakyBucket("t", 100);
Thread.sleep(50);
leak.availablePermits();     // 1 — nothing accumulated
```

Use the leaky bucket when bursts would hurt the downstream system (calling a partner API, consuming a
queue) and the token bucket when a client that has been quiet may legitimately catch up.

## Sliding window

A fixed window resets its counter on a clock boundary, so a client can spend the whole budget just
before the boundary and the whole budget again just after it. A sliding window evaluates the recent
past instead, so that pattern is impossible.

The window is divided into `slots` buckets (10 by default). Each bucket holds a counter tagged with
the window generation it belongs to; a read sums the buckets of the live generations and ignores the
stale ones. Memory is `slots` longs, fixed and independent of traffic.

```
       │◀──────────── rolling window (200ms) ────────────▶│
slots   [ 0 ][ 1 ][ 2 ][ 3 ][ 4 ][ 5 ][ 6 ][ 7 ][ 8 ][ 9 ]
        stale stale  live  live  live  live  live  live  live  now
               └── ignored ──┘└───────── counted ─────────┘
```

Precision is one slot (a tenth of the window by default); pass a larger slot count for finer
precision. A permit is counted until the slot it landed in leaves the window, which is accurate to
within one slot.

Under contention the limiter **fails closed**: a thread reserves its permits, re-reads the total and
rolls its reservation back if the window is already full. Concurrent callers therefore never push the
count past the limit. The cost is that a request can be rejected during a microsecond of racing even
though a permit was about to be released.

## Fixed window

`FixedWindowLimiter` keeps everything in a single `AtomicLong`: the window index in the high 32 bits
and the used permits in the low 32 bits. A check is one atomic read and, at most, one compare-and-set,
which makes it the cheapest limiter to run — the right choice at very high request rates with modest
accuracy requirements.

Its known weakness is the boundary burst: 100 permits at 11:59:59 plus 100 permits at 12:00:01 means
200 permits within two seconds, twice the configured rate. Choose `SlidingWindowLimiter` when a client
could deliberately exploit that.

## What is guaranteed under concurrency

| Guarantee | Token/leaky bucket | Sliding window | Fixed window |
| --- | --- | --- | --- |
| Configured rate can be exceeded | never | never | at a window boundary, up to 2N |
| Requests lost to a race | never | possible (fails closed) | never |
| Mutex or blocking on the fast path | no | no | no |
| Allocations per request | none | none | none |

Both count-based limiters hand out **exactly** the limit under a burst of concurrent callers, which is
what the test suite asserts: 16 threads racing for a limit of 20 grant exactly 20.

## Clock and precision

Time comes from `System.nanoTime()`, which is monotonic: changing the system clock or an NTP jump
cannot make a limiter grant a burst or stall for an hour. Resolution is nanoseconds; real precision is
bounded by the operating system's timer, which is fine for anything above a few hundred permits per
second. For very low rates (a few permits per hour) a limit is enforced to within a scheduling tick,
not to the nanosecond.

## What this library is not

It is a **single-JVM** limiter. Two application instances each get their own bucket, so "100 per
second" means 100 per second per instance. That is the correct behaviour for protecting a process's
own resources, and it is why the library needs no Redis, no network call and no configuration server.

When you need a cluster-wide limit, the usual approach is to keep the fast local limiter in front
(absorbs bursts, no latency) and add a shared counter behind it — Redis with an atomic `INCR` plus
`PEXPIRE`, or a gateway-level limit.
