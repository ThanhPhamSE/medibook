package com.medibook.modules.appointment.service;

public interface RedisLockService {
    boolean acquireLock(String lockKey, long expireTimeSeconds);
    void releaseLock(String lockKey);
}
