package cn.yueshutong.springbootstartercurrentlimiting.monitor;

import cn.yueshutong.springbootstartercurrentlimiting.common.DateTime;
import cn.yueshutong.springbootstartercurrentlimiting.common.ThreadPool;
import cn.yueshutong.springbootstartercurrentlimiting.interceptor.CurrentInterceptorConfig;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Monitor
 * Create by yster@foxmail.com 2019/4/30 0030 16:00
 */
@Component
@ConditionalOnProperty(value = {"current.limiting.monitor.enabled","current.limiting.enabled"}, havingValue = "true")
public class MonitorInterceptor implements HandlerInterceptor {
    @Autowired
    private MonitorService monitorService;

    private final static String NOW = "$_NOW_";

    private Logger logger = LoggerFactory.getLogger(MonitorInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String key = DateTime.now(); //now time
        httpServletRequest.setAttribute(NOW, key);
        ThreadPool.SinglePool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    monitorService.savePre(key);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
            }
        });
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {

    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    public void after(HttpServletRequest request, HttpServletResponse response, Object o, Exception e){
        if (request.getAttribute(NOW)==null){
            return;
        }
        String key = request.getAttribute(NOW).toString();
        request.removeAttribute(NOW);
        ThreadPool.SinglePool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    monitorService.saveAfter(key);
                } catch (Exception ex) {
                    logger.debug(ex.getMessage());
                }
            }
        });
    }
}
