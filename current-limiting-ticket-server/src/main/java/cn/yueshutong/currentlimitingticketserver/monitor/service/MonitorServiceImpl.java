package cn.yueshutong.currentlimitingticketserver.monitor.service;

import cn.yueshutong.currentlimitingticketserver.properties.CurrentMonitorProperties;
import cn.yueshutong.monitor.common.DateTimeUtil;
import cn.yueshutong.monitor.entity.MonitorBean;
import cn.yueshutong.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class MonitorServiceImpl implements MonitorService {
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private CurrentMonitorProperties monitorProperties;

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
            String key = s.getApp() + s.getId()  + s.getName();
            opsForValue.setIfAbsent( PRE + key +"$"+ s.getDate(), String.valueOf(0), monitorProperties.getTime(), TimeUnit.SECONDS);
            opsForValue.increment(PRE  + key +"$"+ s.getDate());
            opsForValue.setIfAbsent( AFTER  + key +"$"+ s.getDate(), String.valueOf(0), monitorProperties.getTime(), TimeUnit.SECONDS);
            opsForValue.increment( AFTER  + key +"$"+ s.getDate());
        });
    }

    @Override
    public List<MonitorBean> getAll(String app, String id, String name) {
        List<MonitorBean> list = new ArrayList<>();
        Set<String> pres = template.keys(PRE + app + id + name + "*");
        if (pres==null){
            return list;
        }
        pres.forEach(s -> {
            String pre = opsForValue.get(s);
            MonitorBean monitorBean = new MonitorBean();
            monitorBean.setApp(app);
            monitorBean.setId(id);
            monitorBean.setName(name);
            monitorBean.setPre(Integer.parseInt(pre));
            String after = opsForValue.get(s.replace(PRE, AFTER));
            monitorBean.setAfter(Integer.parseInt(after));
            monitorBean.setLocalDateTime(DateTimeUtil.parse(s.substring(s.lastIndexOf("$")+1)));
            list.add(monitorBean);
        });
        return list;
    }
}
