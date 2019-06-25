package cn.yueshutong.snowjenaticketserver.token.service;

import cn.yueshutong.commoon.entity.LimiterRule;

public interface TokenService {
    Object token(LimiterRule limiterRule);
}
