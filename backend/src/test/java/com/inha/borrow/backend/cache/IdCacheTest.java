package com.inha.borrow.backend.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class IdCacheTest {
    private JdbcTemplate jdbcTemplate = new JdbcTemplate();
    private IdCache idCache = new IdCache(jdbcTemplate);

    @AfterEach
    void afterEach() {
        idCache.deleteAll();
    }

    @Test
    @DisplayName("대여자가 등록하려는 아이디가 이미 있는 경우")
    void idContainsTest() {
        // given
        String newUser = "test1";
        String oldUser = "test2";
        idCache.setNewUser(newUser);
        idCache.setOldUser(oldUser);
        // when
        boolean newUserResult = idCache.contains(newUser);
        boolean oldUserResult = idCache.contains(oldUser);
        // then
        assertEquals(newUserResult, true);
        assertEquals(oldUserResult, true);
    }

    @Test
    @DisplayName("대여자가 등록하려는 아이디가 없는 경우")
    void idNotContainsTest() {
        // given
        String saveId = "test";
        String testId = "aaa";
        idCache.setNewUser(saveId);
        // when
        boolean result = idCache.contains(testId);
        // then
        assertEquals(result, false);
    }

    @Test
    @DisplayName("대여자가 등록한 아이디의 시간이 만료된 경우")
    void idExpiredTest() {
        // given
        String id = "test";
        idCache.setUserForTest(id, System.currentTimeMillis() - 100000);
        // when
        boolean result = idCache.contains(id);
        // then
        assertEquals(result, false);
        assertNull(idCache.getTtl(id));
    }

    @Test
    @DisplayName("대여자가 탈퇴할 때나 유효기간이 만료된 아이디 제거")
    void removeTest() {
        // given
        String id = "test";
        idCache.setNewUser(id);
        // when
        idCache.remove(id);
        // then
        assertNull(idCache.getTtl(id));
    }

    @Test
    @DisplayName("대여자가 아이디 검증 후 추가적으로 요청(인증번호, 비밀번호 인증)시 아이디 수명 연장")
    void extendTest() {
        // given
        String id = "test";
        idCache.setUserForTest(id, System.currentTimeMillis());
        // when
        idCache.extendTtl(id);
        boolean result = idCache.contains(id);
        // then
        assertEquals(result, true);
    }

    @Test
    @DisplayName("대여자가 모든 인증을 완료하고 회원가입 신청할 때 사용되는 메서드")
    void fixSignUpIdTest() {
        // given
        String id = "test";
        idCache.setNewUser(id);
        // when
        idCache.fixSignUpId(id);
        // then
        long ttl = idCache.getTtl(id);
        assertEquals(ttl, 0l);
    }

    @Test
    @DisplayName("기존 대여자가 아이디를 수정한 경우 캐시에 반영")
    void idReviseTest() {
        // given
        String oldId = "oldId";
        String newId = "newId";
        idCache.setOldUser(newId);
        // when
        idCache.revise(oldId, newId);
        // then
        boolean isOldIdInCache = idCache.contains(oldId);
        boolean isNewIdInCache = idCache.contains(newId);

        assertFalse(isOldIdInCache);
        assertTrue(isNewIdInCache);
    }
}
