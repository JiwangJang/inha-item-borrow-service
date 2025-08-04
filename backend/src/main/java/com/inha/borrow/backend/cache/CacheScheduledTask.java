package com.inha.borrow.backend.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CacheScheduledTask {
    private final IdCache idCache;
    private final SignUpSessionCache signUpSessionCache;
    private final SMSCodeCache smsCodeCache;
    private final int ONE_HOUR = 3_600_000;

    // 시간단위는 ms임, 한시간마다 동작되는 메서드
    @Scheduled(fixedRate = ONE_HOUR)
    public void cleanExpiredCache() {
        idCache.removeOldId();
        signUpSessionCache.removeOldSignUpSession();
        smsCodeCache.removeOldSMSCode();
    }
}
