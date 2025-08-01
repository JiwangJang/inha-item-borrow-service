package com.inha.borrow.backend.authentication.unit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SignUpRequestSessionCache;
import com.inha.borrow.backend.service.BorrowerVerificationService;

@SpringBootTest
public class BorrowerVerificationServiceTest {
    private final BorrowerVerificationService service;
    private final IdCache idCache;
    private final SignUpRequestSessionCache signUpRequestSessionCache;

    public BorrowerVerificationServiceTest(BorrowerVerificationService service, IdCache idCache,
            SignUpRequestSessionCache signUpRequestSessionCache) {
        this.service = service;
        this.idCache = idCache;
        this.signUpRequestSessionCache = signUpRequestSessionCache;
    }

    @Test
    void verifyIdSuccess() {
        // given
        String correctCase = "jiwang917";
        // when
        // then
        service.verifyId(correctCase);

    }
}
