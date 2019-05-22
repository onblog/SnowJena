package cn.yueshutong.currentlimitingticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;

import java.util.List;

public interface RuleService {

    /**
     * 心跳 Heartbeat
     * @param limiterRule 客户端
     * @return 新规则
     */
    LimiterRule heartbeat(LimiterRule limiterRule);

    /**
     * 更新规则一定更新版本号
     * @param limiterRule 参数
     * @return 结果
     */
    boolean update(LimiterRule limiterRule);

    /**
     * 查看所有规则
     * @param name app，id，name
     * @return 规则集合
     */
    List<LimiterRule> getAll(String app,String id,String name);
}
