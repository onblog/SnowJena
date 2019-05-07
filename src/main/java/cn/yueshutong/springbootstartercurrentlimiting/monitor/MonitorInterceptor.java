package cn.yueshutong.springbootstartercurrentlimiting.monitor;

import cn.yueshutong.springbootstartercurrentlimiting.common.DateTime;
import cn.yueshutong.springbootstartercurrentlimiting.common.ThreadPool;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnProperty(prefix = "current.limiting.monitor", name = "enabled", havingValue = "true")
public class MonitorInterceptor implements HandlerInterceptor {
    @Autowired
    private MonitorService monitorService;

    private final static String NOW = "$_NOW_";

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String key = DateTime.now(); //now time
        httpServletRequest.setAttribute(NOW, key);
        ThreadPool.SinglePool.execute(new Runnable() {
            @Override
            public void run() {
                monitorService.savePre(key);
            }
        });
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
        if (request.getAttribute(NOW)==null){
            return;
        }
        String key = request.getAttribute(NOW).toString();
        request.removeAttribute(NOW);
        ThreadPool.SinglePool.execute(new Runnable() {
            @Override
            public void run() {
                monitorService.saveAfter(key);
            }
        });
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }
}
