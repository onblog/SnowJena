# SnowJean使用文档3.0.0

## Maven

```xml
<dependency>
  <groupId>com.github.onblog</groupId>
  <artifactId>snowjena-core</artifactId>
  <version>4.0.0.RELEASE</version>
</dependency>
```

## 本地限流

本项目提供了简单易用的API，对于本地限流，只需要通过工厂模式生产限流器，在需要限流的代码运行之前调用tryAcquire()方法即可。

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 本地限流
     */
    @Test
    public void test1() {
        // 1.配置规则
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setPeriod(1)
                .setUnit(TimeUnit.SECONDS) //每秒令牌数为1
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
        }
    }

}
```

查看控制台打印

```verilog
10:01:40.179 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:41.178 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:42.178 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:43.179 [main] INFO com.example.springbootdemo.AppTest - ok
```

## 黑白名单

黑白名单功能为：对黑名单不允许通过，或只对白名单允许通过（二选一）。

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 黑名单
     */
    @Test
    public void test2() {
        // 1.配置规则
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setRuleAuthority(RuleAuthority.AUTHORITY_BLACK) //黑名单
                .setLimitUser(new String[]{"user1", "user2"})
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire("user1")) {
                logger.info("user1");
            }
            if (limiter.tryAcquire("user2")) {
                logger.info("user2");
            }
            if (limiter.tryAcquire("user3")) {
                logger.info("user3");
            }
        }
    }
    
}
```

查看控制台打印

```verilog
10:04:28.328 [main] INFO com.example.springbootdemo.AppTest - user3
10:04:29.323 [main] INFO com.example.springbootdemo.AppTest - user3
10:04:30.326 [main] INFO com.example.springbootdemo.AppTest - user3
10:04:31.326 [main] INFO com.example.springbootdemo.AppTest - user3
```

## SnowJean-Spring-Boot-Starter

```xml
<dependency>
    <groupId>com.github.onblog</groupId>
    <artifactId>snowjean-spring-boot-starter</artifactId>
    <version>4.0.0.RELEASE</version>
</dependency>
```

### 使用注解

使用注解之前需要生产限流器，并设定限流器的ID，比如如下所示：

``` java
public void register() {
   RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
       .setId("limiter") //ID很重要，对应注解@Limiter中的value
       .setLimit(1)
       .build();
   RateLimiter rateLimiter = RateLimiterFactory.of(rateLimiterRule);
}
```

在项目代码使用注解：

``` java
/**
* Value: rateLimiter.getId()
* fallback: fallback函数名称
*/
@Limiter(value = "limiter", fallback = "sayFallback")
public String say() {
   logger.info("hello");
   return "hello";
}

/**
* 【降级处理】fallback函数的参数要与原函数一致
*/
public String sayFallback() {
   return "fallback_hello";
}
```

## 分布式限流

分布式限流功能相比本地限流更为强大，支持熔断降级，支持动态配置规则，支持可视化监控，开箱即用。 

使用之前你需要：

1. 启动Redis

   本示例默认在本地开启了Redis，默认端口6379，无密码。

2. 启动TicketServer

   克隆本项目，运行 `snowjena-ticket-server` 项目下的 main 方法，默认端口8521。

启动 TicketServer 后的界面：

<img src="http://ww3.sinaimg.cn/large/006tNc79ly1g4i9r01d90j30yl06ngn6.jpg" referrerPolicy="no-referrer"/>

客户端代码：

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 分布式限流
     */
    @Test
    public void test4() throws InterruptedException {
        // 1.限流配置
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setApp("Application")
                .setId("myId")
                .setLimit(1) //每秒1个令牌
                .setBatch(1) //每批次取1个令牌
                .setLimiterModel(LimiterModel.CLOUD) //分布式限流,需启动TicketServer控制台
                .build();
        // 2.配置TicketServer地址（支持集群、加权重）
        Map<String, Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521", 1);
        // 3.全局配置
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        config.setTicketServer(map);
        // 4.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule, config);
        // 5.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
            Thread.sleep(10);
        }
    }
        
}
```

查看控制台打印

```verilog
19:46:47.907 [main] INFO com.example.springbootdemo.AppTest - ok
19:46:48.835 [main] INFO com.example.springbootdemo.AppTest - ok
19:46:49.872 [main] INFO com.example.springbootdemo.AppTest - ok
19:46:50.900 [main] INFO com.example.springbootdemo.AppTest - ok
```

## 监控图表

此时打开你的浏览器，访问 localhost:8521，可以看到控制台视图（1.1.0后的版本需要用户名密码admin）

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia01nyclj31ei0u018x.jpg" referrerPolicy="no-referrer"/>

你可以实时修改限流参数，控制行为，黑/白名单，需要注意的是，黑/白名单列表以英文,逗号分隔。

<img src="http://ww2.sinaimg.cn/large/006tNc79ly1g4ia12hyx0j31ei0u0dw5.jpg" referrerPolicy="no-referrer"/>

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia1dvmasj31ei0u0ngj.jpg" referrerPolicy="no-referrer"/>

你也可以通过监控视图，在线查看系统负载情况。（QPS：每秒收到的请求，PASS：每秒允许的请求）

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia0rnxjmj31ei0u046y.jpg" referrerPolicy="no-referrer"/>

删除该规则下的监控数据

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia1n31wrj31ei0u0wwi.jpg" referrerPolicy="no-referrer"/>

## 限流规则

首先，我们推荐您使用限流规则构造器（LimiterRuleBuilder）生成限流规则，形如以上示例，不过上面的示例只是简要提到了一些限流参数，下面详细说明下 LimiterRule 限流规则。

| 参数          | 说明                      |
| ------------- | ------------------------- |
| App | ApplicationName |
| Id | 限流器ID |
| Unit | 时间单位 |
| Period | 单位时间大小 |
| Limit | 单位时间放入的令牌数 |
| InitialDelay | 第一次放入令牌的延迟时间 |
| Batch | 每批次取多少个令牌 |
| Remaining | 现有令牌数/批次令牌数<=? 取值范围[0,1] |
| Monitor | 监控时长，单位秒，0为关闭 |
| AcquireModel | 控制行为：快速失败/阻塞 |
| LimiterModel | 部署方式：本地/分布式   |
| RuleAuthority | 黑名单/白名单/无          |
| LimitUser     | 黑白名单列表              |

## 设计模式

- 单例模式：全局配置类

- 观察者模式：动态规则配置

- 工厂模式：限流器的生产

- 建造者模式：限流规则的构造


## 高可用

TicketServer支持集群部署，在客户端可对集群进行权重配置，但目前SnowJean使用单点Redis数据库，当Redis挂掉，TicketServer集群也就失去了作用，分布式限流将退化为本地限流。

> 文档不断完善，有问题请提交Issues或者加QQ群。
