package com.github.onblog.snowjenaticketserver.token.service;


import com.github.onblog.commoon.entity.RateLimiterRule;

public interface TokenService {
    Object token(RateLimiterRule rateLimiterRule);
}
