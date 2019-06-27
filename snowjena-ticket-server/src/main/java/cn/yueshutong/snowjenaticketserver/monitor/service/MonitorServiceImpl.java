package cn.yueshutong.snowjenaticketserver.monitor.service;

import cn.yueshutong.monitor.common.DateTimeUtil;
import cn.yueshutong.monitor.entity.MonitorBean;
import cn.yueshutong.monitor.service.MonitorService;
import cn.yueshutong.snowjenaticketserver.exception.ResultEnum;
import cn.yueshutong.snowjenaticketserver.exception.TicketServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MonitorServiceImpl implements MonitorService {
    @Autowired
    private StringRedisTemplate template;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ValueOperations<String, String> opsForValue;

    @PostConstruct
    private void init() {
        opsForValue = template.opsForValue();
    }

    /**
     * Key:APP+PRE/after+time
     * value:i(++)
     */
    @Override
    public void save(List<MonitorBean> monitorBeans) {
        monitorBeans.forEach(s -> {
            opsForValue.increment(MonitorService.getMonitorPreKey(s), s.getPre());
            template.expire(MonitorService.getMonitorPreKey(s), s.getMonitor(), TimeUnit.SECONDS);
            opsForValue.increment(MonitorService.getMonitorAfterKey(s), s.getAfter());
            template.expire(MonitorService.getMonitorAfterKey(s), s.getMonitor(), TimeUnit.SECONDS);
        });
    }

    @Override
    public List<MonitorBean> getAll(String app, String id) {
        if (app == null || id == null) {
            throw new TicketServerException(ResultEnum.ERROR);
        }
        Set<String> pres = template.keys(MonitorService.getMonitorPreKeys(app, id));
        if (pres == null) {
            throw new TicketServerException(ResultEnum.ERROR);
        }
        Map<String, MonitorBean> map = new HashMap<>(); //key：DateTime
        pres.forEach(s -> {
            String pre = opsForValue.get(s);
            MonitorBean monitorBean = new MonitorBean();
            monitorBean.setApp(app);
            monitorBean.setId(id);
            monitorBean.setPre(Integer.parseInt(pre == null ? String.valueOf(0) : pre));
            String after = opsForValue.get(s.replace(PRE, AFTER));
            monitorBean.setAfter(Integer.parseInt(after == null ? String.valueOf(0) : after));
            monitorBean.setLocalDateTime(DateTimeUtil.parse(s.substring(s.lastIndexOf(MonitorService.DATE) + MonitorService.DATE.length())));
            if (map.containsKey(monitorBean.getDateTime())) {
                monitorBean.setPre(monitorBean.getPre() + map.get(monitorBean.getDateTime()).getPre());
                monitorBean.setAfter(monitorBean.getAfter() + map.get(monitorBean.getDateTime()).getAfter());
            }
            map.put(monitorBean.getDateTime(), monitorBean);
        });
        return new ArrayList<>(map.values());
    }

    @Override
    public boolean clean(String app, String id) {
        if (app == null || id == null) {
            throw new TicketServerException(ResultEnum.ERROR);
        }
        Set<String> pres = template.keys(MonitorService.getMonitorPreKeys(app, id));
        if (pres != null) {
            pres.forEach(s -> template.delete(s));
        }
        Set<String> afters = template.keys(MonitorService.getMonitorAfterKeys(app, id));
        if (afters != null) {
            afters.forEach(s -> template.delete(s));
        }
        logger.debug("clean monitor data : " + app + "-" + id);
        return true;
    }

}
