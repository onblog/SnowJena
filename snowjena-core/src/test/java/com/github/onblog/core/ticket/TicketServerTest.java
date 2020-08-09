package com.github.onblog.core.ticket;

import java.util.HashMap;
import java.util.Map;

public class TicketServerTest {

//    @Test
    public void connect() throws InterruptedException {
        TicketServer ticketServer = new TicketServer();
        Map<String,Integer> ip = new HashMap<>();
        ip.put("http://www",1);
        ip.put("http://www.baidu.com",1);
        ticketServer.setServer(ip);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (true) {
                    ticketServer.connect("", "");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        Thread.sleep(10000);
    }
}