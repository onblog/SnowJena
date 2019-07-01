package cn.yueshutong.core.limiter;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.monitor.MonitorServiceImpl;
import cn.yueshutong.monitor.client.MonitorService;
import cn.yueshutong.monitor.entity.MonitorBean;
import com.alibaba.fastjson.JSON;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * 令牌桶算法、漏桶算法
 * 单点、无锁、纳秒级流控
 * 分布式、无锁、秒级流控
 */
public class RateLimiterDefault implements RateLimiter {
    private final AtomicLong bucket = new AtomicLong(0); //令牌桶初始容量：0
    private LimiterRule rule;
    private RateLimiterConfig config;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public MonitorService getMonitorService() {
        return monitorService;
    }

    private MonitorService monitorService = new MonitorServiceImpl();

    RateLimiterDefault(LimiterRule rule, RateLimiterConfig config) {
        this.config = config;
        init(rule);
    }

    @Override
    public void init(LimiterRule rule) {
        this.rule = rule;
        putPointBucket();
    }

    /**
     * 1.黑/白名单
     */
    @Override
    public boolean tryAcquire(String o) {
        boolean allow;
        switch (rule.getRuleAuthority()) {
            case AUTHORITY_BLACK:
                allow = Stream.of(rule.getLimitUser()).noneMatch(s -> s.equals(o));
                break;
            case AUTHORITY_WHITE:
                allow = Arrays.asList(rule.getLimitUser()).contains(o);
                break;
            default:
                allow = true;
        }
        return allow && tryAcquire();
    }

    /**
     * 2.Check
     */
    @Override
    public boolean tryAcquire() {
        if (rule.isEnable()) {
            //限流功能已关闭
            return true;
        }
        return tryAcquireMonitor();
    }


    /**
     * 3.Monitor
     */
    private boolean tryAcquireMonitor() {
        if (rule.getLimiterModel() == LimiterModel.POINT) {
            //本地限流不支持监控
            return tryAcquirePut();
        }
        MonitorBean monitor = new MonitorBean();
        monitor.setLocalDateTime(LocalDateTime.now());
        monitor.setPre(1);
        monitor.setApp(rule.getApp());
        monitor.setId(rule.getId());
        monitor.setName(rule.getName());
        monitor.setMonitor(rule.getMonitor());
        boolean b = tryAcquirePut(); //fact
        if (b) {
            monitor.setAfter(1);
        }
        config.getScheduledThreadExecutor().execute(() -> { //异步执行
            monitorService.save(monitor);
        });
        return b;
    }

    /**
     * 4.putCloudBucket
     */
    private boolean tryAcquirePut() {
        boolean result = tryAcquireFact();
        //分布式方式下检查剩余令牌数
        putCloudBucket();
        return result;
    }

    /**
     * 5.tryAcquireFact
     */
    private boolean tryAcquireFact() {
        if (rule.getLimit() == 0) {
            return false;
        }
        boolean result = false;
        switch (rule.getAcquireModel()) {
            case FAILFAST:
                result = tryAcquireFailed();
                break;
            case BLOCKING:
                result = tryAcquireSucceed();
                break;
        }
        return result;
    }

    /**
     * CAS获取令牌,没有令牌立即失败
     */
    private boolean tryAcquireFailed() {
        long l = bucket.longValue();
        while (l > 0) {
            if (bucket.compareAndSet(l, l - 1)) {
                return true;
            }
            l = bucket.longValue();
        }
        return false;
    }

    /**
     * CAS获取令牌,阻塞直到成功
     */
    private boolean tryAcquireSucceed() {
        long l = bucket.longValue();
        while (!(l > 0 && bucket.compareAndSet(l, l - 1))) {
            sleep();
            l = bucket.longValue();
        }
        return true;
    }

    /**
     * 线程休眠
     */
    private void sleep() {
        //大于1ms强制休眠
        if (rule.getUnit().toMillis(rule.getPeriod()) < 1) {
            return;
        }
        try {
            Thread.sleep(rule.getUnit().toMillis(rule.getPeriod()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 本地限流，放入令牌
     */
    private void putPointBucket() {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        if (rule.getLimit() == 0 || !rule.getLimiterModel().equals(LimiterModel.POINT)) {
            return;
        }
        this.scheduledFuture = config.getScheduledThreadExecutor().scheduleAtFixedRate(() -> bucket.set(rule.getLimit()), rule.getInitialDelay(), rule.getPeriod(), rule.getUnit());
    }

    /**
     * 集群限流，取批令牌
     */
    private void putCloudBucket() {
        //校验
        if (!rule.getLimiterModel().equals(LimiterModel.CLOUD) ||
                bucket.get() / 1.0 * rule.getBatch() > rule.getRemaining()) {
            return;
        }
        //异步任务
        config.getScheduledThreadExecutor().execute(() -> {
            //DCL,再次校验
            if (bucket.get() / 1.0 * rule.getBatch() <= rule.getRemaining()) {
                synchronized (bucket) {
                    if (bucket.get() / 1.0 * rule.getBatch() <= rule.getRemaining()) {
                        String result = config.getTicketServer().connect(RateLimiterConfig.http_token, JSON.toJSONString(rule));
                        if (result != null) {
                            bucket.getAndAdd(Long.parseLong(result));
                        }
                    }
                }
            }
        });
    }

    @Override
    public String getId() {
        return rule.getId();
    }

    @Override
    public LimiterRule getRule() {
        return this.rule;
    }


}
