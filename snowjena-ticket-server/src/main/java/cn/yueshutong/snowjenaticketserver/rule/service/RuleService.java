package cn.yueshutong.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.rule.entity.Result;

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
     * 查看规则
     * @param name app，id，name
     * @param page
     * @param limit
     * @return 规则集合
     */
    Result<LimiterRule> getAll(String app, String id, String name, int page, int limit);
}
