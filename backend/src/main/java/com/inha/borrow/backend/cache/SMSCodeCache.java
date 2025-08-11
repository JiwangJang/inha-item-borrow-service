package com.inha.borrow.backend.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.entity.SMSCode;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

/**
 * 대여자의 핸드폰 인증코드를 잠시 저장하는 클래스
 * 
 * @author 장지왕
 */
@Component
public class SMSCodeCache {
    private final ConcurrentHashMap<String, SMSCode> cache = new ConcurrentHashMap<>();

    /**
     * 특정 대여자의 인증코드를 가져오는 메서드
     * 
     * @param id 대여자 아이디
     * @return BorrowerPhoneVerificationCode
     */
    public SMSCode get(String id) {
        SMSCode smsCode = cache.get(id);
        if (smsCode == null) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());

        }
        if (smsCode.getTtl() <= System.currentTimeMillis()) {
            ApiErrorCode errorCode = ApiErrorCode.SMS_CODE_EXPIRED;
            remove(id);
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
        return smsCode;
    }

    public void removeOldSMSCode() {
        cache.forEach((id, smsCode) -> {
            long ttl = smsCode.getTtl();
            if (ttl <= System.currentTimeMillis()) {
                cache.remove(id);
            }
        });
    }

    /**
     * 특정 대여자의 인증코드를 저장하는 메서드
     * 
     * @param id   대여자 아이디
     * @param code 인증코드 객체
     */
    public void set(String id, SMSCode code) {
        cache.put(id, code);
    }

    /**
     * 특정 대여자의 인증코드를 지우는 메서드
     * <p>
     * 유효시간이 만료됐을때나 인증이 완료됐을때 삭제
     * 
     * @param id 대여자 아이디
     */
    public void remove(String id) {
        cache.remove(id);
    }

    // 테스트용 메서드
    /*
     * 테스트를 위해 유효기간을 임의로 지정해 캐시에 저장하는 메서드
     */
    public void setForTest(String id, SMSCode code) {
        cache.put(id, code);
    }

    /**
     * 테스트 후 모두 지워버리는 메서드
     */
    public void deleteAll() {
        cache.clear();
    }
}
