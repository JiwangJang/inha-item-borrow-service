package com.inha.borrow.backend.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.entity.SignUpSession;
import com.inha.borrow.backend.model.exception.InvalidValueException;

public class SignUpSessionCacheTest {
    private IdCache idCache = new IdCache(new JdbcTemplate());
    private SignUpSessionCache cache = new SignUpSessionCache(idCache);

    @AfterEach
    void afterEach() {
        cache.deleteAll();
        idCache.deleteAll();
    }

    @Test
    @DisplayName("SignUpSession 저장 및 읽기 테스트")
    void setSignUpSessionTest() {
        // given
        String id = "test";
        cache.set(id);
        // when
        SignUpSession result = cache.get(id);
        // then
        assertEquals(result.isIdCheck(), true);
        assertEquals(result.isPasswordCheck(), false);
        assertEquals(result.isPhoneCheck(), false);
        assertThat(result.getTtl()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("SignUpSession 삭제 테스트")
    void deleteSignUpSessionTest() {
        // given
        String id = "test";
        cache.set(id);
        // when
        cache.remove(id);
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get(id);
        });
    }

    @Test
    @DisplayName("만료된 SignUpSession 읽기 테스트")
    void getExpiredSignUpSessionTest() {
        // given
        String id = "test";
        cache.setForTest(id, System.currentTimeMillis());
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            cache.get(id);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get(id);
        });
    }

    @Test
    @DisplayName("저장되지 않은 SignUpSession 읽기 테스트")
    void getNotExistSignUpSessionTest() {
        // given
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get("not_exist_id");
        });
    }

    @Test
    @DisplayName("핸드폰 인증 확인 표시 테스트(유효한 세션)")
    void phoneCheckSuccessForValidSessionTest() {
        // given
        String id = "test";
        cache.setForTest(id, System.currentTimeMillis() + 300000);
        // when
        cache.phoneCheckSuccess(id);
        SignUpSession session = cache.get(id);
        // then
        assertTrue(session.isPhoneCheck());
        assertThat(session.getTtl()).isGreaterThan(System.currentTimeMillis() + 590000);
    }

    @Test
    @DisplayName("핸드폰 인증 확인 표시 테스트(만료된 세션)")
    void phoneCheckSuccessForExpiredSessionTest() {
        // given
        String id = "test";
        cache.setForTest(id, System.currentTimeMillis());
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            cache.phoneCheckSuccess(id);
        });
    }

    @Test
    @DisplayName("비밀번호 검증 표시 테스트(유효한 세션)")
    void passwordCheckSuccessForValidSessionTest() {
        // given
        String id = "test";
        cache.setForTest(id, System.currentTimeMillis() + 300000);
        // when
        cache.passwordCheckSuccess(id);
        SignUpSession session = cache.get(id);
        // then
        assertTrue(session.isPasswordCheck());
        assertThat(session.getTtl()).isGreaterThan(System.currentTimeMillis() + 590000);
    }

    @Test
    @DisplayName("비밀번호 검증 표시 테스트(만료된 세션")
    void passwordCheckSuccessForExpiredSessionTest() {
        // given
        String id = "test";
        cache.setForTest(id, System.currentTimeMillis());
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            cache.passwordCheckSuccess(id);
        });
    }

    @Test
    @DisplayName("회원가입 신청전 모든 조건 통과했는지 테스트(성공)")
    void isAllPassedSuccessTest() {
        // given
        String id = "test1";
        // when
        cache.set(id);
        cache.passwordCheckSuccess(id);
        cache.phoneCheckSuccess(id);
        boolean result = cache.isAllPassed(id);
        // then
        assertTrue(result);
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.get(id);
        });
        assertThat(idCache.getTtl(id)).isZero();
    }

    @Test
    @DisplayName("회원가입 신청전 모든 조건 통과했는지 테스트(실패-비번통과 못함)")
    void isAllPassedFailForNotPassPWTest() {
        // given
        String id = "test1";
        // when
        cache.set(id);
        cache.phoneCheckSuccess(id);
        boolean result = cache.isAllPassed(id);
        SignUpSession session = cache.get(id);
        // then
        assertFalse(result);
        assertFalse(session.isPasswordCheck());
        assertThat(idCache.getTtl(id)).isGreaterThan(0);
    }

    @Test
    @DisplayName("회원가입 신청전 모든 조건 통과했는지 테스트(실패-핸드폰 통과 못함)")
    void isAllPassedFailForNotPassPhoneTest() {
        // given
        String id = "test1";
        // when
        cache.set(id);
        cache.passwordCheckSuccess(id);
        boolean result = cache.isAllPassed(id);
        SignUpSession session = cache.get(id);
        // then
        assertFalse(result);
        assertFalse(session.isPhoneCheck());
        assertThat(idCache.getTtl(id)).isGreaterThan(0);
    }

    @Test
    @DisplayName("회원가입 신청전 모든 조건 통과했는지 테스트(실패-아이디 부존재)")
    void isAllPassedFailForNotExistIdTest() {
        // given
        String id = "test1";
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            cache.isAllPassed(id);
        });
    }

    @Test
    @DisplayName("회원가입 신청전 모든 조건 통과했는지 테스트(실패-세션만료)")
    void isAllPassedFailForSignUpSessionExpiredTest() {
        // given
        String id = "test1";
        // when
        cache.setForTest(id, System.currentTimeMillis());
        // then
        assertThrows(InvalidValueException.class, () -> {
            cache.isAllPassed(id);
        });
    }

}
