package cn.yueshutong;

import cn.yueshutong.config.ThreadManager;
import cn.yueshutong.enums.AcquireModel;
import cn.yueshutong.enums.Algorithm;
import cn.yueshutong.enums.RuleAuthority;
import cn.yueshutong.propertry.LimiterRule;
import cn.yueshutong.rateLimiter.RateLimiter;
import cn.yueshutong.rateLimiter.RateLimiterFactory;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void shouldAnswerWithTrue() throws InterruptedException {
        assertTrue(true);
        long f = 1;
        Thread.sleep(f / 10);
        long d = f / 10;
        System.out.println(d);
    }

    @Test
    public void should() {
        LimiterRule rule = new LimiterRule();
        rule.setId("Default");
        rule.setAcquireModel(AcquireModel.BLOCKING);
        rule.setRuleAuthority(RuleAuthority.AUTHORITY_BLACK);
        rule.setQps(0.2);
        String s = JSON.toJSONString(rule);
        System.out.println(s);
        Object parse = JSON.parseObject(s, LimiterRule.class);
        System.out.println(parse);
    }

    @Test
    public void shoulds() {
        ThreadManager.setScheduled(Executors.newScheduledThreadPool(3));
        ScheduledFuture<?> scheduledFuture = ThreadManager.getScheduled().scheduleAtFixedRate(() -> {
            System.out.println("one" + LocalDateTime.now());
        }, 0, 1, TimeUnit.SECONDS);
        ScheduledFuture<?> scheduledFuture1 = ThreadManager.getScheduled().scheduleAtFixedRate(() -> {
            System.out.println("two" + LocalDateTime.now());
        }, 0, 2, TimeUnit.SECONDS);
        try {
            Thread.sleep(1000 * 6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduledFuture.cancel(true);
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tess() throws InterruptedException {
        LimiterRule rule = new LimiterRule();
        rule.setQps(2);
        rule.setAlgorithm(Algorithm.TOKENBUCKET);
        rule.setAcquireModel(AcquireModel.BLOCKING);
        RateLimiter limiter = RateLimiterFactory.of(rule);
        Thread.sleep(1000);
        while (true) {
            if (limiter.tryAcquire()) {

            }
            sayHi();
        }
    }

    private void sayHi() {
        logger.info("hi");
    }
}
