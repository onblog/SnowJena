package cn.yueshutong.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadManager {
    private static ScheduledExecutorService scheduled; //线程池

    public static ScheduledExecutorService getScheduled() {
        return scheduled == null? Executors.newScheduledThreadPool(1):scheduled;
    }

    public static void setScheduled(ScheduledExecutorService scheduled) {
        ThreadManager.scheduled = scheduled;
    }
}
