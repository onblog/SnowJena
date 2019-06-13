# SnowJean Documentation

## Maven

```xml
<dependency>
  <groupId>cn.yueshutong</groupId>
  <artifactId>snowjena-core</artifactId>
  <version>1.0.0.RELEASE</version>
</dependency>
```

## A single point of current limit 

This project provides an easy-to-use API. For a single point of current limitation, it is only necessary to produce the current limiter through factory mode and call the tryAcquire() method before the code that needs to be limited runs. 

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test1(){
        // 1.configuration rule
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setQps(1); //Concurrency per second
        // 2.Factory mode production of current limiter
        RateLimiter limiter = RateLimiterFactory.of(limiterRule);
        // 3.use
        while (true){
            if (limiter.tryAcquire()){
                logger.info("ok");
            }
        }
    }

}
```

View console print :

```verilog
15:10:06.670 [main] INFO com.example.springbootdemo.AppTest - ok
15:10:07.651 [main] INFO com.example.springbootdemo.AppTest - ok
15:10:08.653 [main] INFO com.example.springbootdemo.AppTest - ok
15:10:09.652 [main] INFO com.example.springbootdemo.AppTest - ok
```

## blacklist and whitelist 

Black and white list function: black list is not allowed to pass, or only allow the white list to pass (either). 

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * blacklist and whitelist
     */
    @Test
    public void test2(){
        // 1.configuration rule
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setQps(1);
        limiterRule.setRuleAuthority(RuleAuthority.AUTHORITY_BLACK);
        limiterRule.setLimitApp(new String[]{"user1","user2"});
        // 2.Factory mode production of current limiter
        RateLimiter limiter = RateLimiterFactory.of(limiterRule);
        // 3.use
        while (true){
            if (limiter.tryAcquire("user1")){
                logger.info("user1");
            }
            if (limiter.tryAcquire("user2")){
                logger.info("user2");
            }
            if (limiter.tryAcquire("user3")){
                logger.info("user3");
            }
        }
    }
    
}
```

View console print 

```verilog
15:19:22.506 [main] INFO com.example.springbootdemo.AppTest - user3
15:19:23.522 [main] INFO com.example.springbootdemo.AppTest - user3
15:19:24.484 [main] INFO com.example.springbootdemo.AppTest - user3
```

## Distributed current limiting

Distributed current limiting function is more powerful than single point current limiting function, which supports fusing degradation, flow shaping, dynamic configuration rules and visual monitoring, out of the box.

You need to:

1. start Redis

   In this example, Redis is enabled locally by default, with default port 6379 and no password. 

2. start TicketServer

   Download the ticketserver.jar and run on the default port 8521. 

Interface after launching TicketServer: ![1559635413548](./picture/1559635413548.png)

Client-Side Code

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test4() {
        // 1.Current limiting configuration
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setApp("Application");
        limiterRule.setId("myId");
        limiterRule.setQps(1);
        limiterRule.setLimiterModel(LimiterModel.CLOUD);
        // 2.configuration TicketServer location（Support for clustering and weighting）
        Map<String,Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521",1);
        // 3.Global Configuration
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        config.setTicketServer(map);
        // 4.Factory mode production of current limiter
        RateLimiter limiter = RateLimiterFactory.of(limiterRule, config);
        // 5.use
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
        }
    }
        
}
```

View console print 

```verilog
16:07:08.694 [main] INFO com.example.springbootdemo.AppTest - ok
16:07:09.660 [main] INFO com.example.springbootdemo.AppTest - ok
16:07:10.643 [main] INFO com.example.springbootdemo.AppTest - ok
```

## Deshboard

At this point, open your browser and go to localhost:8521 to see the console view 

![1559636053811](./picture/1559636053811.png)

You can modify QPS in real time, control behavior, current limiting algorithm, black and white list, it should be noted that the list list in English, comma separated. 

![1559636229219](./picture/1559636229219.png)

You can also view the system load online by monitoring the view. (QPS: request received, PASS: request allowed) 

![1559636333481](./picture/1559636333481.png)



## Current limiting rules 

Some functions have been briefly mentioned above, while the LimiterRule is explained in detail below. 

| parameter     | explain                                       |
| ------------- | --------------------------------------------- |
| initialDelay  | The initial time delay                        |
| acquireModel  | Control behavior: fast failure/constant speed |
| algorithm     | Algorithm: token bucket/leak bucket           |
| limiterModel  | Deployment mode: single point/distributed     |
| ruleAuthority | Blacklist/whitelist/none                      |
| limitApp      | Black and white list                          |

> Documentation continues to improve. Please submit Issues. 