package cn.yueshutong.snowjenaticketserver.thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class ThreadManager {
    @Autowired
    private ThreadProperties properties;

    @Bean
    public ScheduledExecutorService get(){
        ScheduledExecutorService scheduledThreadExecutor = Executors.newScheduledThreadPool(properties.getSize());
        return scheduledThreadExecutor;
    }

}
