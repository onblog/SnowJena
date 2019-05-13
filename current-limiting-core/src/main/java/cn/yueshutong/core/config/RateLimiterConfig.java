package cn.yueshutong.core.config;

import cn.yueshutong.core.ticket.TicketServer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Config
 */
public class RateLimiterConfig {
    private String app; //App name
    private ScheduledExecutorService scheduled; //线程池
    private TicketServer ticketServer; //发票服务器

    public String getApp() {
        assert app!=null;
        return app;
    }

    public void setApp(String app) {
        this.app = app;
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
