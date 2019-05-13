package cn.yueshutong.springbootstartercurrentlimiting.current;

import cn.yueshutong.springbootstartercurrentlimiting.common.ThreadPool;
import cn.yueshutong.springbootstartercurrentlimiting.current.flag.MyCurrentInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentRuleHandler;
import cn.yueshutong.springbootstartercurrentlimiting.interceptor.CurrentInterceptorConfig;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.MonitorInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentRecycleProperties;
import cn.yueshutong.springbootstartercurrentlimiting.property.CurrentProperty;
import cn.yueshutong.springbootstartercurrentlimiting.rateLimiter.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 自定义扩展限流规则：参数由用户实现CurrentRuleHandler接口指定
 */
@Component
@ConditionalOnBean(value = {CurrentRuleHandler.class, CurrentInterceptorConfig.class})
public class CustomCurrentInterceptor implements HandlerInterceptor, MyCurrentInterceptor {
    @Autowired
    private CurrentRecycleProperties recycleProperties;

    @Autowired(required = false)
    private MonitorInterceptor monitorInterceptor;

    @Autowired(required = false)
    private CurrentInterceptorHandler interceptorHandler;

    @Autowired(required = false)
    private CurrentRuleHandler limiterRule;

    private Map<String, RateLimiter> map = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        this.memoryLeak(); //执行过期对象回收
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CurrentProperty property = limiterRule.rule(request); //读取实现的规则
        if (property == null) { //为NULL则默认不限制
            return true;
        }
        RateLimiter rateLimiter = initRateLimiter(property); //初始化限流器
        if (property.isFailFast()) { //执行快速失败
            return tryAcquireFailed(request, response, handler, rateLimiter);
        } else { //执行阻塞策略
            return rateLimiter.tryAcquire();
        }
    }

    /**
     * Initialize the current limiter
     * 为了提高性能，不加同步锁，所以刚开始可能存在极短暂的误差。
     */
    private RateLimiter initRateLimiter(CurrentProperty property) {
        if (!map.containsKey(property.getId())) {
            map.put(property.getId(), RateLimiter.of(property.getQps(), property.getInitialDelay(), SpringContextUtil.getApplicationName() + property.getId(), property.isOverflow(), property.getTime(), property.getUnit()));
        }
        return map.get(property.getId());
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response, Object handler, RateLimiter rateLimiter) throws Exception {
        if (rateLimiter.tryAcquireFailed()) { //To get the token
            return true;
        }
        //No token was taken
        if (interceptorHandler == null) {
            response.setStatus(403);
            response.getWriter().print(RateLimiter.message);
        } else {
            interceptorHandler.preHandle(request, response);
        }
        return false;
    }

    /**
     * 解决大规模限流器注册而长时间不使用导致的内存泄漏问题，定时删除过期的限流器对象，秒级。
     */
    private void memoryLeak() {
        if (!recycleProperties.isEnabled()){ //no use
            return;
        }
        ThreadPool.scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.debug("Recovering current limiting object...");
                Iterator<Map.Entry<String, RateLimiter>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, RateLimiter> entry = iterator.next();
                    RateLimiter value = entry.getValue();
                    LocalDateTime expirationTime = value.getExpirationTime();
                    if (expirationTime != null && expirationTime.isBefore(LocalDateTime.now())) {
                        iterator.remove();
                    }
                }
            }
        }, recycleProperties.getTime(), recycleProperties.getTime(), TimeUnit.SECONDS);
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //Before the view is rendered
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //After the entire request is completed
        if (monitorInterceptor!=null){
            monitorInterceptor.after(httpServletRequest,httpServletResponse,o,e);
        }
    }

}

