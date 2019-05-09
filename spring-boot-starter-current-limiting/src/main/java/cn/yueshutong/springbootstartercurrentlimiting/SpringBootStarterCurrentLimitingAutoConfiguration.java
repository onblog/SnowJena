package cn.yueshutong.springbootstartercurrentlimiting;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
@ConditionalOnWebApplication
@ComponentScan
public class SpringBootStarterCurrentLimitingAutoConfiguration {

    /**
     * Redis-Lua
     */
    @Bean
    @ConditionalOnProperty(prefix = "current.limiting", name = "cloud-enabled", havingValue = "true")
    public DefaultRedisScript<Long> defaultRedisScript() {
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/putbucket.lua")));
        return defaultRedisScript;
    }

}
