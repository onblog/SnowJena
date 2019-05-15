package cn.yueshutong.core.config;

import cn.yueshutong.core.ticket.TicketServer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 单例模式
 * DCL双检查锁
 */
public class RateLimiterConfig {
    private static RateLimiterConfig rateLimiterConfig; //实例
    private ScheduledExecutorService scheduled; //调度线程池
    private TicketServer ticketServer; //发票服务器

    private RateLimiterConfig(){
        //禁止new实例
    }

    public static RateLimiterConfig getInstance(){
        if (rateLimiterConfig==null){
            synchronized (RateLimiterConfig.class){
                if (rateLimiterConfig==null) {
                    rateLimiterConfig = new RateLimiterConfig();
                }
            }
        }
        return rateLimiterConfig;
    }

    public ScheduledExecutorService getScheduled() {
        if (scheduled==null){
            setScheduled(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()*2));
        }
        return scheduled;
    }

    public void setScheduled(ScheduledExecutorService scheduled) {
        this.scheduled = scheduled;
    }

    public TicketServer getTicketServer() {
        assert ticketServer!=null;
        return ticketServer;
    }

    public void setTicketServer(Map<String,Integer> ip) {
        if (this.ticketServer==null){
            synchronized (this) {
                if (this.ticketServer==null) {
                    this.ticketServer = new TicketServer();
                }
            }
        }
        this.ticketServer.setIp(ip);
    }
}
