package com.inha.borrow.backend.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CacheScheduledTask {
    private static final int ONE_HOUR = 3_600_000;

    // 시간단위는 ms임, 한시간마다 동작되는 메서드
    @Scheduled(fixedRate = ONE_HOUR)
    public void cleanExpiredCache() {
        log.info("[Scheduled] 캐시 정리작업 수행");
    }
}
