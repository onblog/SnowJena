package cn.yueshutong.redislock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private DefaultRedisScript<Long> redisScript;

    private static final Long RELEASE_SUCCESS = 1L;

    private long timeout = 5000;

    public boolean lock(String key, String value) {
        long start = System.currentTimeMillis();
        while (true) {
            //检测是否超时
            if (System.currentTimeMillis() - start > timeout) {
                return false;
            }
            //执行set命令
            Boolean absent = template.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.MILLISECONDS);//1
            //其实没必要判NULL，这里是为了程序的严谨而加的逻辑
            if (absent == null) {
                return false;
            }
            //是否成功获取锁
            if (absent) {
                return true;
            }
        }
    }

    public boolean unlock(String key, String value) {
        //使用Lua脚本：先判断是否是自己设置的锁，再执行删除
        Long result = template.execute(redisScript, Arrays.asList(key,value));
        //返回最终结果
        return RELEASE_SUCCESS.equals(result);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Bean
    public DefaultRedisScript<Long> defaultRedisScript() {
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText("if redis.call('get', KEYS[1]) == KEYS[2] then return redis.call('del', KEYS[1]) else return 0 end");
        return defaultRedisScript;
    }

}
