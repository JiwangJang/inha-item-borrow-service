package com.inha.borrow.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SMSCodeCache;
import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.model.entity.SMSCode;
import com.inha.borrow.backend.model.entity.SignUpSession;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@SpringBootTest
public class BorrowerVerificationServiceTest {
    private BorrowerVerificationService service;
    private IdCache idCache;
    private SMSCodeCache smsCodeCache;
    private SignUpSessionCache signUpSessionCache;

    @Autowired
    public BorrowerVerificationServiceTest(BorrowerVerificationService service, IdCache idCache,
            SMSCodeCache smsCodeCache, SignUpSessionCache signUpSessionCache) {
        this.service = service;
        this.idCache = idCache;
        this.smsCodeCache = smsCodeCache;
        this.signUpSessionCache = signUpSessionCache;
    }

    @AfterEach
    void afterEach() {
        idCache.deleteAll();
        smsCodeCache.deleteAll();
        signUpSessionCache.deleteAll();
    }

    @Test
    @DisplayName("아이디 검증 테스트(성공)")
    void verifyIdSuccessTest() {
        // given
        String testId = "test1";
        // when
        service.verifyId(testId);
        long idCacheResult = idCache.getTtl(testId);
        SignUpSession session = signUpSessionCache.get(testId);
        // then
        assertThat(idCacheResult).isEqualTo(session.getTtl());
        assertTrue(session.isIdCheck());
    }

    @Test
    @DisplayName("아이디 검증 테스트(이미존재하는 아이디)")
    void verifyIdFailForExistIdTest() {
        // given
        String testId = "test1";
        String existId = testId;
        service.verifyId(testId);
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            service.verifyId(existId);
        });
    }

    @Test
    @DisplayName("비밀번호 검증 테스트(성공)")
    void verifyPasswordSuccessTest() {
        // given
        String id = "test1";
        String password = "!^&*()_-+=Aa1";
        // when
        service.verifyId(id);
        service.verifyPassword(id, password);
        SignUpSession session = signUpSessionCache.get(id);
        // then
        assertTrue(session.isPasswordCheck());
        assertTrue(session.isIdCheck());
        assertThat(idCache.getTtl(id)).isEqualTo(session.getTtl());
    }

    @Test
    @DisplayName("휴대폰 인증코드 발송 테스트(성공)")
    void sendSMSCodeSuccessTest() {
        // given
        String id = "test1";
        // when
        service.verifyId(id);
        service.sendSMSCode(id, "000-0000-0000");
        SMSCode codeSession = smsCodeCache.get(id);
        SignUpSession signUpSession = signUpSessionCache.get(id);
        Long idTtl = idCache.getTtl(id);
        // then
        assertEquals(codeSession.getCode(), "123456");
        assertThat(codeSession.getTtl()).isGreaterThan(System.currentTimeMillis() + 170000);
        assertThat(signUpSession.getTtl()).isEqualTo(idTtl);
    }

    @Test
    @DisplayName("휴대폰 인증코드 발송 테스트(실패-없는 사람)")
    void sendSMSCodeFailForNotFoundSignUpSessionTest() {
        // given
        String id = "test1";
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            service.sendSMSCode(id, "000-0000-0000");
        });
    }

    @Test
    @DisplayName("휴대폰 인증코드 발송 테스트(실패-만료된 회원가입 세션)")
    void sendSMSCodeFailForExpiredSignUpSessionTest() {
        // given
        String id = "test1";
        // when
        signUpSessionCache.setForTest(id, System.currentTimeMillis());
        // then
        assertThrows(InvalidValueException.class, () -> {
            service.sendSMSCode(id, "000-0000-0000");
        });
    }

    @Test
    @DisplayName("휴대폰 인증코드 검증 테스트(성공)")
    void verifySMSCodeSuccessTest() {
        // given
        String id = "test1";
        service.verifyId(id);
        service.sendSMSCode(id, "000-0000-0000");
        // when
        service.verifySMSCode(id, "123456");
        SignUpSession signUpSession = signUpSessionCache.get(id);
        Long idTtl = idCache.getTtl(id);
        // then
        assertTrue(signUpSession.isIdCheck());
        assertTrue(signUpSession.isPhoneCheck());
        assertThat(signUpSession.getTtl()).isEqualTo(idTtl);
    }

    @Test
    @DisplayName("휴대폰 인증코드 검증 테스트(실패-코드 다름)")
    void verifySMSCodeFailForIncorrectCodeTest() {
        // given
        String id = "test1";
        service.verifyId(id);
        service.sendSMSCode(id, "000-0000-0000");
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            service.verifySMSCode(id, "111111");
        });
        SignUpSession signUpSession = signUpSessionCache.get(id);
        assertTrue(signUpSession.isIdCheck());
        assertFalse(signUpSession.isPhoneCheck());
    }

    @Test
    @DisplayName("휴대폰 인증코드 검증 테스트(실패-아이디 인증 안한 사용자)")
    void verifySMSCodeFailForNotPassIdCheckTest() {
        // given
        String id = "test1";
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            service.verifySMSCode(id, "111111");
        });
    }
}
