package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.budgee.service.TokenBlackListService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TOKEN-BLACKLIST-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlackListServiceImpl implements TokenBlackListService {

    StringRedisTemplate redisTemplate;

    @Override
    public void blackListToken(String refreshToken, Instant expiration) {
        log.info(
                "[blackListToken] refreshToken={} expiration={}",
                refreshToken.substring(0, 10),
                expiration);

        long secondsToExpire = expiration.getEpochSecond() - Instant.now().getEpochSecond();
        if (secondsToExpire > 0) {
            redisTemplate
                    .opsForValue()
                    .set("blacklist:" + refreshToken, "revoked", secondsToExpire, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isBlackListed(String refreshToken) {
        log.info("[isBlackListed] refreshToken={}", refreshToken.substring(0, 10));

        return redisTemplate.hasKey("blacklist:" + refreshToken);
    }
}
