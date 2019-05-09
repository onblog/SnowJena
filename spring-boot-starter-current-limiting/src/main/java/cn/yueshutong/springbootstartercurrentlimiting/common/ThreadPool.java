package cn.yueshutong.springbootstartercurrentlimiting.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Create by yster@foxmail.com 2019/5/1 0001 15:26
 */
public interface ThreadPool {

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(SpringContextUtil.getCorePoolSize());

    ExecutorService SinglePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

}
