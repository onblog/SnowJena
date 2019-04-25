![](./picture/logo.jpg)

## 1.简介

基于令牌桶算法和漏桶算法实现的纳秒级分布式无锁限流插件，完美嵌入SpringBoot、SpringCloud应用，支持接口限流、方法限流、系统限流、IP限流、用户限流等规则，支持设置系统启动保护时间（保护时间内不允许访问），提供快速失败与CAS阻塞两种限流方案，开箱即用。

![1555848355646](./picture/yulan.png)

## 2.Maven

```xml
<dependency>
  <groupId>cn.yueshutong</groupId>
  <artifactId>spring-boot-starter-current-limiting</artifactId>
  <version>0.0.5.RELEASE</version>
</dependency>
```

## 3.方法限流

在需要限流的方法上使用 @CurrentLimiter 注解，不局限于Controller方法，示例代码如下：

```java
@RestController
public class MyController {

    @RequestMapping("/hello")
    @CurrentLimiter(QPS = 2)
    public String hello(){
        return "hello";
    }

}
```

@CurrentLimiter 注解参数说明：

| 属性         | 说明         | 默认值 |
| ------------ | ------------ | ------ |
| QPS          | 每秒并发量| 20     |
| initialDelay | 初始延迟时间| 0      |
| failFast     | 是否快速失败 | true   |
| overflow | 是否严控速率 | false |

关于属性的具体说明请参见下方介绍。

## 4.系统限流

对整个应用的限流只需要在配置文件中配置即可，示例代码如下：

```properties
current.limiting.enabled=true #开启系统限流
current.limiting.part-enabled=false #使限流注解的作用失效
current.limiting.qps=100 #每秒并发量，支持小数、分数。计算规则：次数/时间(秒级)
current.limiting.fail-fast=true #快速失败
current.limiting.initial-delay=0 #系统启动保护时间为0
current.limiting.overflow=true #切换为漏桶算法
```

参数说明：

| 属性          | 说明         | 默认值 |
| ------------- | ------------ | ------ |
| enabled       | 是否开启非注解的限流器 | FALSE  |
| part-enabled  | 是否开启注解限流的作用。FALSE可使注解限流失效 | TRUE   |
| qps           | 每秒并发量。支持小数，计算规则：次数/时间(秒级)，为0禁止访问 | 100    |
| fail-fast     | 是否执行快速失败，FALSE可切换为阻塞 | TRUE   |
| initial-delay | 首次放入令牌（即允许访问）的延迟时间，可作为系统启动保护，单位:毫秒 | 0      |
| overflow | 是否严格控制请求速率和次数，TRUE即切换为漏桶算法 | false |
| recycling | 间隔多少秒可以去回收过期的限制器对象 | 10 |
| corePoolSize | 执行计划任务的线程池中的核心线程数 | 10 |

## 5.拒绝策略

提供快速失败与CAS阻塞两种限流方案。如果是阻塞则不需要拒绝策略，当获取到令牌后依旧会继续执行，可以当做一种限制速率的措施。这里只讨论快速失败的拒绝策略。

快速失败的默认策略是统一返回“服务不可用”的英文说明文字，如果用户需要自定义拒绝策略，提供两种接口供实现。

针对被注解的方法进行自定义拒绝策略是实现CurrentAspectHandler接口，示例代码：

```java
@Component
public class MyAspectHandler implements CurrentAspectHandler {
    @Override
    public Object around(ProceedingJoinPoint pjp, CurrentLimiter rateLimiter) throws Throwable {
        //被注解修饰的方法返回值，慎用！
        //可以结合Controller返回自定义视图
        return "fail";
    }
}
```

针对系统级别的拒绝策略是实现CurrentInterceptorHandler接口，示例代码：

```java
@Component
public class MyInterceptorHandler implements CurrentInterceptorHandler {
    @Override
    public void preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.getWriter().print("fail");
    }

}
```
需要注意的是，以上实现类在Application中只能注入一个。

## 6.集群限流

集群限流的目的是对相同实例（即ApplicationName）的集群进行统一的限流，前提是已经开启并配置好Redis，直接开启即可：

```properties
spring.redis.host=127.0.0.1
spring.redis.password=
spring.redis.port=6379
#一行开启
current.limiting.cloud-enabled=true
```
例如QPS为2，两个实例组成集群，效果图如下：

![](./picture/jiqun1.jpg)

![](./picture/jiqun2.jpg)

## 7.自定义限流规则
在实际场景中，我们的限流规则并不只是简单的对整个系统或单个接口进行流控，需要考虑的是更复杂的场景。例如：

1. 对请求的目标URL进行限流（例如：某个URL每分钟只允许调用多少次）
2. 对客户端的访问IP进行限流（例如：某个IP每分钟只允许请求多少次）
3. 对某些特定用户或者用户组进行限流（例如：非VIP用户限制每分钟只允许调用100次某个API等）
4. 多维度混合的限流。此时，就需要实现一些限流规则的编排机制。与、或、非等关系。

在本插件中实现这些业务需求是非常轻量简便的，只需要一个实现 CurrentRuleHandler 接口并返回CurrentProperty 对象的 Bean 即可。示例代码如下：

```java
@Component
public class MyRule implements CurrentRuleHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public CurrentProperty rule(HttpServletRequest request) {
        request.getServletPath(); // /hello
        request.getMethod(); // GET
        request.getRemoteHost(); // 127.0.0.1
        request.getSession(); // session
        //返回NULL是无规则的意思，即放行。
        return CurrentPropertyFactory.of("Default",3,0,true,true);
    }
}
```

CurrentProperty 构造方法参数说明：

| 参数         | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| id           | 标识名。若为IP地址则为IP地址限流，若为用户名则为用户限流，若为访问的URL则为接口限流。 |
| qps          | 每秒并发量。支持小数、分数，计算规则：次数/时间(秒)。为0禁止访问。 |
| initialDelay | 首次放入令牌（即允许访问）延迟时间，可作为系统启动保护时间，单位/毫秒。 |
| failFast     | 是否需开启快速失败。false即切换为阻塞。                      |
| overflow     | 是否严格控制请求速率和次数，true即切换为漏桶算法。               |
| time |该规则限流器对象的存活时间（选填） |
| unit |该规则限流器对象的存活时间单位（选填） |

例如：对接口进行限流，只需要 request.getServletPath() 作为参数 id 的值即可。

**注意：一旦自定义规则，即实现CurrentRuleHandler接口，那么系统默认配置的限流规则会失效。注解限流和拦截限流是可以同时作用的。**

## 8.更新日志

0.0.1.RELEASE：单点限流，注解+全局配置。

0.0.2.RELEASE：结合Redis实现集群限流。内部使用选举算法选出Master节点。

0.0.3.RELEASE：可自定义规则限流、增加令牌桶算法与漏桶算法的切换，纳秒级并发控制。

0.0.4.RELEASE：解决大规模限流器注册而长时间不使用导致的内存泄漏问题，定时删除过期的限流器对象，秒级。统一线程池管理，并可定义核心线程数。

0.0.5.RELEASE：去掉集群限流器的锁操作，改进令牌桶算法，实现真正的无锁限流。使用享元模式减少大量对象的创建。

## 9.关于作者

博客：[http://www.yueshutong.cn](http://www.yueshutong.cn/)

邮箱：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/spring-boot-starter-current-limiting>

Gitee：<https://gitee.com/zyzpp/spring-boot-starter-current-limiting>

交流QQ群：781927207
