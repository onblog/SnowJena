package cn.yueshutong.springbootstartercurrentlimiting.property;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 享元模式
 */
public class CurrentPropertyFactory {
    private static Map<String,CurrentProperty> map = new WeakHashMap<>();

    /**
     * @param id 标识名，若为IP地址则为IP地址限流，若为用户名则为用户限流，若为访问的URL则为接口限流。
     * @param qps 每秒并发量，支持小数、分数，计算规则：次数/时间(秒)。为0则禁止访问。
     * @param initialDelay 首次放入令牌延迟时间，可作为系统启动保护时间，毫秒。
     * @param failFast 是否需开启快速失败。
     * @param overflow 是否切换为漏桶算法。
     * @param time 时间
     * @param unit 时间单位
     */
    public static CurrentProperty of(String id, double qps, long initialDelay, boolean failFast, boolean overflow, long time, ChronoUnit unit){
        CurrentProperty property = map.get(id);
        if (property==null){
            property = new CurrentProperty(id,qps,initialDelay,failFast,overflow,time,unit);
            map.put(id,property);
        }
        return property;
    }

    /**
     * @param id 标识名，若为IP地址则为IP地址限流，若为用户名则为用户限流，若为访问的URL则为接口限流。
     * @param qps 每秒并发量，支持小数、分数，计算规则：次数/时间(秒)。为0则禁止访问。
     * @param initialDelay 首次放入令牌延迟时间，可作为系统启动保护时间，毫秒。
     * @param failFast 是否需开启快速失败。
     * @param overflow 是否切换为漏桶算法。
     */
    public static CurrentProperty of(String id, double qps, long initialDelay, boolean failFast, boolean overflow){
        return of(id,qps,initialDelay,failFast,overflow,0,null);
    }

}
