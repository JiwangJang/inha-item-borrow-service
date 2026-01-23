package com.inha.borrow.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;

import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@JdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ StudentCouncilFeeVerificationRepository.class })
@Slf4j
public class StudentCouncilFeeVerificationRepositoryTest {
    // 인증하는거 없어서 안돌아감 -> 나중에 인증 하는거 완료되고 돌려봐야함

    @Autowired
    private StudentCouncilFeeVerificationRepository repository;

    @Test
    @DisplayName("initialSave 메서드 테스트")
    void testInitialSave() {
        // given
        String testId = "11111111";

        // when
        repository.initialSave(testId);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then
        assertNotNull(result);
        assertEquals(testId, result.getId());
    }

    @Test
    @DisplayName("verificationRequestSave 메서드 테스트")
    void testVerificationRequestSave() {
        // given
        String testId = "11111111";
        String s3Link = "s3://bucket/image.jpg";

        // when
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Link);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then

        assertEquals(s3Link, result.getS3Link());
        assertNotNull(result.getRequestAt());
        assertNull(result.isVerify());
    }

    @Test
    @DisplayName("findAllRequests 메서드 테스트")
    void testFindAllRequests() {
        // given
        repository.initialSave("11111111");
        repository.initialSave("2222222");
        repository.verificationRequestSave("11111111", "s3://link1.jpg");
        repository.verificationRequestSave("2222222", "s3://link2.jpg");

        // when
        List<StudentCouncilFeeVerification> results = repository.findAllRequests();

        // then
        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }

    @Test
    @DisplayName("updateForAdmin 메서드 테스트")
    void testUpdateForAdmin() {
        // given
        repository.initialSave("11111111");
        repository.verificationRequestSave("11111111", "s3://link2.jpg");

        // when
        repository.updateForAdmin("11111111", false, "reason");
        StudentCouncilFeeVerification result = repository.findRequestById("11111111");

        // then
        assertFalse(result.isVerify());
    }

    @Test
    @DisplayName("cancel 메서드 테스트")
    void testCancel() {
        // given
        String testId = "11111111";
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, "s3://image.jpg");

        // when
        repository.cancel(testId);

        // then
        StudentCouncilFeeVerification result = repository.findRequestById(testId);
        assertNull(result.getS3Link());
        assertNull(result.getRequestAt());
        assertNull(result.isVerify());
    }
}
