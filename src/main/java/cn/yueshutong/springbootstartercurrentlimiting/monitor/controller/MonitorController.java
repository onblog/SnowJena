package cn.yueshutong.springbootstartercurrentlimiting.monitor.controller;

import cn.yueshutong.springbootstartercurrentlimiting.monitor.MonitorInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.entity.MonitorBean;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by yster@foxmail.com 2019/5/4 0004 12:46
 */
@Controller
@ConditionalOnBean(MonitorInterceptor.class)
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @ResponseBody
    @RequestMapping(value = "/queryall")
    public List<MonitorBean> queryAll(){
        return monitorService.queryAll();
    }

}
