package cn.yueshutong.springbootstartercurrentlimiting.property;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 享元模式
 */
public class CurrentPropertyFactory {
    private static Map<String,CurrentProperty> map = new WeakHashMap<>();

    public static CurrentProperty of(String id, double qps, long initialDelay, boolean failFast, boolean overflow, long time, ChronoUnit unit){
        CurrentProperty property = map.get(id);
        if (property==null){
            property = new CurrentProperty(id,qps,initialDelay,failFast,overflow,time,unit);
            map.put(id,property);
        }
        return property;
    }

    public static CurrentProperty of(String id, double qps, long initialDelay, boolean failFast, boolean overflow){
        return of(id,qps,initialDelay,failFast,overflow,0,null);
    }

}
