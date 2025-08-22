package com.inha.borrow.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenService = new JwtTokenService();
        Field secretKeyField = JwtTokenService.class.getDeclaredField("JWT_SECRET_KEY");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtTokenService, "test-secret-key-123456789012345678901234567890"); // 최소 길이 32 이상
    }

    @Test
    void createAndValidateToken() {
        String userId = "user123";
        String token = jwtTokenService.createToken(userId);

        assertThat(token).isNotNull();
        boolean valid = jwtTokenService.validateToken(token);
        assertThat(valid).isTrue();

        String extractedId = jwtTokenService.getUserId(token);
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        String invalidToken = "invalid.token.value";
        boolean valid = jwtTokenService.validateToken(invalidToken);
        assertThat(valid).isFalse();
    }
}
