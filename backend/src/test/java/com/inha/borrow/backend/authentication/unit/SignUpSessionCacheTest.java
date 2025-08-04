package com.inha.borrow.backend.authentication.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.model.auth.SignUpSession;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.exception.SignUpSessionExpiredException;

public class SignUpSessionCacheTest {
    private SignUpSessionCache cache = new SignUpSessionCache();

    @AfterEach
    void afterEach() {
        cache.deleteAll();
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
        assertThrows(SignUpSessionExpiredException.class, () -> {
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
        assertThrows(SignUpSessionExpiredException.class, () -> {
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
        assertThrows(SignUpSessionExpiredException.class, () -> {
            cache.passwordCheckSuccess(id);
        });
    }

    // isAllPass 메서드 테스트 할 것 + 추가 동작(아이디 영구 전환 및 SignUpSession 삭제)
}
