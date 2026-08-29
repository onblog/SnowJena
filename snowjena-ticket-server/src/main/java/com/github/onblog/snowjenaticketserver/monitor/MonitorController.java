package com.github.onblog.snowjenaticketserver.monitor;


import com.github.onblog.snowjenaticketserver.exception.ResultEnum;
import com.github.onblog.snowjenaticketserver.exception.ResultException;
import com.github.onblog.snowjenaticketserver.rule.entity.Result;
import com.alibaba.fastjson.JSON;
import com.github.onblog.monitor.entity.MonitorBean;
import com.github.onblog.monitor.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class MonitorController {
    @Autowired
    private MonitorService monitorService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/monitor", method = RequestMethod.POST)
    @ResponseBody
    public ResultEnum monitor(@RequestParam("data") String rule) {
        logger.debug("monitor up: " + rule);
        List<MonitorBean> monitorBeans = JSON.parseArray(rule, MonitorBean.class);
        monitorService.save(monitorBeans);
        return ResultEnum.SUCCESS;
    }

    @RequestMapping(value = "/monitor/json", method = RequestMethod.GET)
    @ResponseBody
    @ResultException
    public Result<MonitorBean> query(String app, String id) {
        List<MonitorBean> monitorBeans = monitorService.getAll(app, id);
        monitorBeans.sort(null);
        Result<MonitorBean> result = new Result<>(ResultEnum.SUCCESS);
        result.setData(monitorBeans);
        return result;
    }

    @RequestMapping(value = "/monitor", method = RequestMethod.DELETE)
    @ResponseBody
    @ResultException
    public Result delete(String app, String id) {
        monitorService.clean(app, id);
        return new Result(ResultEnum.SUCCESS);
    }
}
