package com.medibook.modules.appointment.service.impl;

import com.medibook.modules.appointment.service.RedisLockService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("test")
@Slf4j
public class InMemoryLockServiceImpl implements RedisLockService {

    private final ConcurrentHashMap<String, Boolean> locks = new ConcurrentHashMap<>();

    @Override
    public boolean acquireLock(String lockKey, long expireTimeSeconds) {
        // putIfAbsent returns null if key didn't exist (i.e. lock acquired successfully)
        boolean acquired = locks.putIfAbsent(lockKey, Boolean.TRUE) == null;
        log.debug("InMemoryLock acquire key: {} -> {}", lockKey, acquired);
        return acquired;
    }

    @Override
    public void releaseLock(String lockKey) {
        locks.remove(lockKey);
        log.debug("InMemoryLock release key: {}", lockKey);
    }
}
