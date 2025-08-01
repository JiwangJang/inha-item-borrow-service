package com.inha.borrow.backend.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheScheduledTask {
    // IdCache idCache

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCache() {

    }
}
