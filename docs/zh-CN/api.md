# API 参考

所有类都在同一个包：`io.github.onblog.limiter`。jar 里声明了
`Automatic-Module-Name: io.github.onblog.limiter`，可直接用于模块路径。

## `RateLimiter`（接口）

```java
public interface RateLimiter {
    String name();
    int availablePermits();
    default boolean tryAcquire();
    boolean tryAcquire(int permits);
    default boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException;
}
```

| 方法 | 语义 |
| --- | --- |
| `name()` | 构造时传入的名字，用于日志与指标。 |
| `availablePermits()` | "此刻还能放行多少"的快照，取值 `0` ~ 配置上限。**仅用于观测**，不能用来做限流判断。 |
| `tryAcquire()` | 申请 1 个许可，等价于 `tryAcquire(1)`。 |
| `tryAcquire(int)` | 原子申请全部许可：要么全给，要么全不给。需要拒绝时返回 `false`；`permits < 1` 抛 `IllegalArgumentException`。 |
| `tryAcquire(int, long, TimeUnit)` | 阻塞等待，直到拿到许可或超时。被中断时抛 `InterruptedException`（并清除中断标志）。 |

被限流是正常结果：返回 `false`，既不抛异常也不打日志。

## `RateLimiters`（工厂）

```java
public final class RateLimiters {
    TokenBucketLimiter   tokenBucket(String name, int permitsPerSecond);
    TokenBucketLimiter   tokenBucket(String name, int capacity, int permitsPerWindow, Duration window);
    LeakyBucketLimiter   leakyBucket(String name, int permitsPerSecond);
    LeakyBucketLimiter   leakyBucket(String name, int permitsPerWindow, Duration window);
    SlidingWindowLimiter slidingWindow(String name, int maxPermits, Duration window);
    FixedWindowLimiter   fixedWindow(String name, int maxPermits, Duration window);
    WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int permitsPerSecond, Duration warmUpPeriod);
    WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int capacity, int permitsPerWindow,
                                               Duration window, Duration warmUpPeriod);
    ConcurrencyLimiter   concurrency(String name, int maxConcurrency);
}
```

令牌桶和漏桶的单参数重载表示"每秒"；`tokenBucket` 的第一个重载等价于 `capacity == permitsPerSecond`。

## `TokenBucketLimiter`

```java
new TokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window);
int capacity();
```

平均速率为 `permitsPerWindow / window`，允许最多 `capacity` 个许可的突发。`capacity` 同时也是单次请求最多能申请的数量。

## `LeakyBucketLimiter`

```java
new LeakyBucketLimiter(String name, int permitsPerWindow, Duration window);
int permitsPerWindow();
```

每 `window / permitsPerWindow` 放行一个许可，严格均匀、不累积突发。桶里始终只有一个许可，所以申请 2 个及以上必然被拒绝。

## `SlidingWindowLimiter`

```java
public static final int DEFAULT_SLOTS = 10;

new SlidingWindowLimiter(String name, int maxPermits, Duration window);
new SlidingWindowLimiter(String name, int maxPermits, Duration window, int slotCount);
int maxPermits();
```

任意滚动的 `window` 内最多 `maxPermits` 个。`slotCount`（默认 10）是精度/内存的旋钮：窗口被切成这么多份，精度为 `window / slotCount`。

## `FixedWindowLimiter`

```java
new FixedWindowLimiter(String name, int maxPermits, Duration window);
int maxPermits();
```

每个对齐窗口内最多 `maxPermits` 个（窗口起点是一个固定原点的整数倍）。实现最省，但跨窗口边界最多可能放过 2 倍上限。

## `WarmUpTokenBucketLimiter`

```java
public static final int COLD_FACTOR = 3;

new WarmUpTokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window,
                             Duration warmUpPeriod);
int capacity();
int permitsPerWindow();
int coldFactor();
double warmUpProgress();     // 启动时为 0.0，预热完成后为 1.0
```

速率从 `1 / COLD_FACTOR` 线性爬升到配置值的令牌桶，突发额度同步从 `capacity/COLD_FACTOR` 涨到 `capacity`。爬升依据是限流器的**运行时长**而不是流量：空闲的限流器也会自己预热完。`warmUpProgress()` 可用于看板与测试。

## `ConcurrencyLimiter`

```java
new ConcurrencyLimiter(String name, int maxConcurrency);
int maxConcurrency();
int inFlight();
void release();
void release(int permits);
Permit tryEnter();            // 满了返回 null
Permit tryEnter(int permits); // 槽位不够返回 null
void execute(Runnable task);
<T> T call(Callable<T> task);

interface Permit extends AutoCloseable {
    @Override void close();   // 幂等
}
```

限制"同时在飞的许可数"。与速率限流不同，这里的许可是**槽位，必须归还**：只 `tryAcquire()` 不 `release()` 会让上限永久缩水。所以优先用 try-with-resources 的 `tryEnter()`，或者用 `execute()`/`call()`（它们在任务前后自动申请与归还）。`Permit.close()` 重复调用是幂等的，不会把上限越关越大。

槽位全满时 `execute()`/`call()` 抛 `IllegalStateException`；`call()` 还会把任务抛出的受检异常包成 `IllegalStateException`（原始异常作为 cause）。

## 异常

| 异常 | 触发条件 |
| --- | --- |
| `IllegalArgumentException` | 构造参数不为正、名字为空、窗口为零/负数/`null`，或 `tryAcquire(permits)` 的 `permits < 1`。消息会指明是哪个参数，例如 `capacity must be > 0 but was 0`。 |
| `InterruptedException` | 阻塞版 `tryAcquire(permits, timeout, unit)` 被中断。中断标志已被清除，若不想向上传播，记得在自己的 `catch` 里恢复它。 |
| `IllegalStateException` | `ConcurrencyLimiter.release(n)` 归还的槽位多于持有数（重复释放 bug），或 `execute()`/`call()` 在槽位全满时被调用。 |

拒绝永远不抛异常。任何方法都不打日志。

## 线程安全

所有实现的配置字段不可变，运行期只操作原子变量，因此可以在线程间自由共享，也可以安全地放进 `static final` 字段或容器 Bean。

限流器不是 `Serializable`，也不能从状态重建：它是运行期对象，不是配置项。

## 单次调用开销

| 限流器 | 快路径 |
| --- | --- |
| `TokenBucketLimiter`、`LeakyBucketLimiter` | 1 次原子读 + 1 次 CAS |
| `FixedWindowLimiter` | 1 次原子读 + 1 次 CAS |
| `SlidingWindowLimiter` | `slotCount` 次原子读 + 1 次 CAS，外加一次回滚校验读 |
| `WarmUpTokenBucketLimiter` | 1 次原子读 + 1 次 CAS（爬升曲线由时钟现算） |
| `ConcurrencyLimiter` | 1 次原子读 + 1 次 CAS |

无论放行还是拒绝都不分配对象；阻塞版只是把线程 park 住，同样不分配。
