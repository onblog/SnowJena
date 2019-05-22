package cn.yueshutong.currentlimitingticketserver;

import cn.yueshutong.redislock.RedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrentLimitingTicketServerApplicationTests {

    @Autowired
    private RedisLock lock;

    @Test
    public void contextLoads() throws InterruptedException {
        System.out.println(lock.lock("key","value"));
        Thread.sleep(4000);
        System.out.println(lock.unlock("key","value"));
    }

}
