package cn.yueshutong.snowjenaticketserver.monitor.service;

import cn.yueshutong.monitor.common.DateTimeUtil;
import cn.yueshutong.monitor.entity.MonitorBean;
import cn.yueshutong.monitor.service.MonitorService;
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

    private static final String PRE = "$PRE$";
    private static final String AFTER = "$AFTER$";

    /**
     * Key:APP+PRE/after+time
     * value:i(++)
     * setnx+expire
     */
    @PostConstruct
    private void init() {
        opsForValue = template.opsForValue();
    }

    @Override
    public void save(List<MonitorBean> monitorBeans) {
        monitorBeans.forEach(s -> {
            String key = s.getApp() + s.getId() + s.getName();
            opsForValue.set(PRE + key + "$" + s.getDateTime(), String.valueOf(s.getPre()), s.getMonitor(), TimeUnit.SECONDS);
            opsForValue.set(AFTER + key + "$" + s.getDateTime(), String.valueOf(s.getAfter()), s.getMonitor(), TimeUnit.SECONDS);
        });
    }

    @Override
    public List<MonitorBean> getAll(String app, String id, String name) {
        if (app == null || id == null) {
            return new ArrayList<>();
        }
        String builder = app + id + (name == null ? "" : name);
        Set<String> pres = template.keys(PRE + builder);
        if (pres == null) {
            return new ArrayList<>();
        }
        Map<String, MonitorBean> map = new HashMap<>(); //key：DateTime
        pres.forEach(s -> {
            String pre = opsForValue.get(s);
            MonitorBean monitorBean = new MonitorBean();
            monitorBean.setApp(app);
            monitorBean.setId(id);
            monitorBean.setName(name);
            monitorBean.setPre(Integer.parseInt(pre == null ? String.valueOf(0) : pre));
            String after = opsForValue.get(s.replace(PRE, AFTER));
            monitorBean.setAfter(Integer.parseInt(after == null ? String.valueOf(0) : after));
            monitorBean.setLocalDateTime(DateTimeUtil.parse(s.substring(s.lastIndexOf("$") + 1)));
            if (map.containsKey(monitorBean.getDateTime())) {
                monitorBean.setPre(monitorBean.getPre() + map.get(monitorBean.getDateTime()).getPre());
                monitorBean.setAfter(monitorBean.getAfter() + map.get(monitorBean.getDateTime()).getAfter());
            }
            map.put(monitorBean.getDateTime(), monitorBean);
        });
        return new ArrayList<>(map.values());
    }
}
