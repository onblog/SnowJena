package com.github.onblog.snowjenaticketserver.thread;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ticket.server.thread-pool")
public class ThreadProperties {
    private int size = Runtime.getRuntime().availableProcessors();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
