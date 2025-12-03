package com.ssginc.showpingrefactoring.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {
    private final StringRedisTemplate redis;
    private final String prefix;
    private final int limitPerMinute;


    public RateLimiter(StringRedisTemplate redis,
                       @Value("${redis.prefix:sp:mfa:}") String prefix,
                       @Value("${mfa.rateLimitPerMinute:5}") int limitPerMinute){
        this.redis = redis; this.prefix = prefix; this.limitPerMinute = limitPerMinute;
    }


    public void check(String key){
        String k = prefix + "rate:" + key;
        Long v = redis.opsForValue().increment(k);
        if (v != null && v == 1L) redis.expire(k, Duration.ofMinutes(1));
        if (v != null && v > limitPerMinute) throw new IllegalStateException("Too many attempts");
    }
}
