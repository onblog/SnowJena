package cn.yueshutong.redislock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create by yster@foxmail.com 2019/5/26 0026 18:44
 */
//定义为配置类
@Configuration
//在web工程条件下成立
@ConditionalOnWebApplication
//前提导入Redis依赖
@ConditionalOnClass(RedisTemplate.class)
//扫描
@ComponentScan
public class RedisLockAutoConfiguration {
}
