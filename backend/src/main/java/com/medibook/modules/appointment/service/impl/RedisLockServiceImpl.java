package com.medibook.modules.appointment.service.impl;

import com.medibook.modules.appointment.service.RedisLockService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("!test")
@Slf4j
public class RedisLockServiceImpl implements RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean acquireLock(String lockKey, long expireTimeSeconds) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", expireTimeSeconds, TimeUnit.SECONDS);
            return success != null && success;
        } catch (Exception e) {
            log.error("Failed to acquire Redis lock for key: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("Failed to release Redis lock for key: {}", lockKey, e);
        }
    }
}
