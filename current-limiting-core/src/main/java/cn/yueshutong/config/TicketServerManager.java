package cn.yueshutong.config;

import cn.yueshutong.ticket.TicketServer;

import java.util.Map;

/**
 * 发票服务器（注册/获取/更新）
 */
public class TicketServerManager {
    private static TicketServer ticketServer;

    public static TicketServer getTicketServer() {
        assert ticketServer!=null;
        return ticketServer;
    }

    public static void setTicketServer(TicketServer ticketServer) {
        TicketServerManager.ticketServer = ticketServer;
    }

    public static void update(Map<String, Integer> ip) {
        TicketServerManager.ticketServer.setIp(ip);
    }
}
