package cn.yueshutong.springbootstartercurrentlimiting.current;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.current.flag.MyCurrentInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.MonitorInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentMonitorProperties;
import cn.yueshutong.springbootstartercurrentlimiting.rateLimiter.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentRuleHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentRuleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 系统默认限流规则：通过配置文件指定参数
 * Create by yster@foxmail.com 2019/4/21 0021 11:19
 */
@Component
@ConditionalOnMissingBean(value = CurrentRuleHandler.class)
public class DefaultCurrentInterceptor implements HandlerInterceptor, MyCurrentInterceptor {
    @Autowired
    private CurrentRuleProperties limiterRule;

    @Autowired(required = false)
    private MonitorInterceptor monitorInterceptor;

    @Autowired(required = false)
    private CurrentInterceptorHandler interceptorHandler;

    private RateLimiter rateLimiter;

    @PostConstruct
    public void init(){
        this.rateLimiter = RateLimiter.of(limiterRule.getQps(), limiterRule.getInitialDelay(), SpringContextUtil.getApplicationName(), limiterRule.isOverflow());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (limiterRule.isFailFast()) { //执行快速失败
            return tryAcquireFailed(request, response);
        } else { //执行阻塞策略
            return rateLimiter.tryAcquire();
        }
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (rateLimiter.tryAcquireFailed()) { //To get the token
            return true;
        } else { //No token was taken
            if (interceptorHandler == null) {
                response.setStatus(403);
                response.getWriter().print(RateLimiter.message);
            } else {
                interceptorHandler.preHandle(request, response);
            }
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {
        //Before the view is rendered
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //After the entire request is completed
        if (monitorInterceptor!=null){
            monitorInterceptor.after(httpServletRequest,httpServletResponse,o,e);
        }
    }
}