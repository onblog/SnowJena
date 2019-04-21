![](./picture/logo.jpg)

## 1.简介

spring-boot-starter-current-limiting：完美嵌入SpringBoot应用的**无锁限流**插件，支持方法级别、系统级别限流，支持设置系统启动保护时间，提供快速失败与CAS阻塞两种限流方案，这些功能只需要导入依赖，简单配置即可使用。

![1555848355646](./picture/yulan.png)

## 2.Maven

```xml
<dependency>
  <groupId>cn.yueshutong</groupId>
  <artifactId>spring-boot-starter-current-limiting</artifactId>
  <version>0.0.1.RELEASE</version>
</dependency>
```

## 3.方法限流

在需要限流的方法上使用 @CurrentLimiter 注解，示例代码如下：

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
| QPS          | 每秒并发量   | 20     |
| initialDelay | 初始延迟时间<br>系统启动保护 | 0      |
| failFast     | 开启快速失败<br>可切换为阻塞 | true   |

## 4.系统限流

对整个应用的限流只需要在配置文件中配置即可，示例代码如下：

```properties
current.limiting.enabled=true
current.limiting.part-enabled=false
current.limiting.qps=100
current.limiting.fail-fast=true
current.limiting.initial-delay=0
```

参数说明：

| 属性          | 说明         | 默认值 |
| ------------- | ------------ | ------ |
| enabled       | 开启全局限流 | false  |
| part-enabled  | 开启注解限流<br>可使注解失效 | true   |
| qps           | 每秒并发量   | 100    |
| fail-fast     | 开启快速失败<br>可切换为阻塞 | true   |
| initial-delay | 初始延迟时间<br>系统启动保护 | 0      |

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

## 6.关于作者

博客：[http://www.yueshutong.cn](http://www.yueshutong.cn/)

邮箱：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/spring-boot-starter-current-limiting>

Gitee：<https://gitee.com/zyzpp/spring-boot-starter-current-limiting>

交流QQ群：781927207