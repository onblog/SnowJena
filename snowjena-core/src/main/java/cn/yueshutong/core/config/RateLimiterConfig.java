package cn.yueshutong.core.config;

import cn.yueshutong.core.exception.SnowJeanException;
import cn.yueshutong.core.ticket.TicketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 单例模式
 * DCL双检查锁
 */
public class RateLimiterConfig {
    private static RateLimiterConfig rateLimiterConfig; //单例
    private static Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);

    private TicketServer ticketServer; //发票服务器
    private ScheduledExecutorService scheduledThreadExecutor; //调度线程池

    //Ticket server interface
    public static String http_monitor = "monitor";
    public static String http_heart = "heart";
    public static String http_token = "token";

    private RateLimiterConfig() {
        //禁止new实例
    }

    public static RateLimiterConfig getInstance() {
        if (rateLimiterConfig == null) {
            synchronized (RateLimiterConfig.class) {
                if (rateLimiterConfig == null) {
                    rateLimiterConfig = new RateLimiterConfig();
                    logger.info("Starting [SnowJean]");
                }
            }
        }
        return rateLimiterConfig;
    }

    public ScheduledExecutorService getScheduledThreadExecutor() {
        if (this.scheduledThreadExecutor == null) {
            synchronized (this) {
                if (this.scheduledThreadExecutor == null) {
                    setScheduledThreadExecutor(new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, new ThreadPoolExecutor.DiscardOldestPolicy()));
                }
            }
        }
        return this.scheduledThreadExecutor;
    }

    public void setScheduledThreadExecutor(ScheduledExecutorService scheduledThreadExecutor) {
        this.scheduledThreadExecutor = scheduledThreadExecutor;
    }

    public TicketServer getTicketServer() {
        if (ticketServer == null) {
            throw new SnowJeanException("error: ticketServer == null");
        }
        return ticketServer;
    }

    public void setTicketServer(Map<String, Integer> ip) {
        if (ip.size() < 1) {
            throw new SnowJeanException("ip.size()<1 is not pass!");
        }
        if (this.ticketServer == null) {
            synchronized (this) {
                if (this.ticketServer == null) {
                    this.ticketServer = new TicketServer();
                }
            }
        }
        this.ticketServer.setServer(ip);
    }

}
