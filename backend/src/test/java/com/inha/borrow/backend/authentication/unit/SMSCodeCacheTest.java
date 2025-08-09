package com.inha.borrow.backend.authentication.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.inha.borrow.backend.cache.SMSCodeCache;
import com.inha.borrow.backend.model.auth.SMSCode;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.exception.InvalidValueException;

public class SMSCodeCacheTest {
    private SMSCodeCache cache = new SMSCodeCache();
    String codeStr = "123456";
    String id = "test";

    @AfterEach
    void afterEach() {
        cache.deleteAll();
    }

    @Test
    @DisplayName("핸드폰 인증코드 저장 및 읽기 테스트")
    void setAndReadSmsCodeTest() {
        // given
        SMSCode code = new SMSCode(codeStr);
        // when
        cache.set(id, code);
        // then
        SMSCode result = cache.get(id);
        assertEquals(result.getCode(), codeStr);
        assertThat(result.getTtl()).isGreaterThan(System.currentTimeMillis() + 170000);
    }

    @Test
    @DisplayName("만료된 핸드폰 인증코드 읽기 테스트")
    void readExpiredSmsCodeTest() {
        // given
        SMSCode code = new SMSCode(codeStr, System.currentTimeMillis());
        // when
        cache.set(id, code);
        // then
        assertThrows(InvalidValueException.class, () -> {
            cache.get(id);
        });
    }

    @Test
    @DisplayName("존재하지 않는 핸드폰 인증코드 읽기 테스트")
    void readNotExistSmsCodeRead() {
        // given
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get(id);
        });
    }

    @Test
    @DisplayName("핸드폰 인증코드 삭제 테스트")
    void deleteSmsCodeTest() {
        // given
        SMSCode code = new SMSCode(codeStr);
        // when
        // then
        cache.set(id, code);
        SMSCode testCode = cache.get(id);
        assertEquals(testCode.getCode(), codeStr);

        cache.remove(id);
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get(id);
        });

    }
}
