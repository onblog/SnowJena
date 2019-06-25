package cn.yueshutong.snowjenaticketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TicketServerApplication {

    public static void main(String[] args) {
        System.out.println("SnowJean Version 1.1.1.RELEASE");
        SpringApplication.run(TicketServerApplication.class, args);
    }

}