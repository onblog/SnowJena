package cn.yueshutong.snowjenaticketserver.token.service;

import cn.yueshutong.commoon.entity.RateLimiterRule;

public interface TokenService {
    Object token(RateLimiterRule rateLimiterRule);
}
