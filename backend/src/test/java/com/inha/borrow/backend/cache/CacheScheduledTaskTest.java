package com.inha.borrow.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class CacheScheduledTaskTest {

    @Autowired
    private CacheScheduledTask cacheScheduledTask;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private StudentCouncilFeeVerificationRepository verificationRepository;

    @Autowired
    private Cache<String, CacheBorrowerDto> borrowerCache;

    @BeforeEach
    void setUp() {
        borrowerCache.invalidateAll();
        // Repository 초기화 로직 (필요 시)
    }

    @Test
    @DisplayName("DB 데이터를 조합하여 캐시를 갱신한다")
    void refreshBorrowerCache_Success() {
        // Given: DB에 사용자 1명과 해당 사용자의 학생회비 납부 정보 저장
        String userId = "20241234";

        // 1. 사용자 저장 (BorrowerDto 사용 등 환경에 맞춰 수정)
        borrowerRepository.save(BorrowerDto.builder()
                .id(userId)
                .name("민재")
                .phonenumber("01010001000")
                .accountNumber("123123213")
                .build());

        // 2. 학생회비 납부 정보 저장
        verificationRepository.initialSave(userId);
        verificationRepository.verificationRequestSave(userId,"https://s3.link/test");

        // When: 스케줄링 메서드 강제 호출
        cacheScheduledTask.refreshBorrowerCache();

        // Then: 캐시 확인
        CacheBorrowerDto cachedData = borrowerCache.getIfPresent(userId);

        assertThat(cachedData).isNotNull();
        assertThat(cachedData.getName()).isEqualTo("민재");
        assertThat(cachedData.isVerify()).isTrue(); // 합쳐진 정보 검증
        assertThat(cachedData.getS3Link()).isEqualTo("https://s3.link/test");
    }

    @Test
    @DisplayName("학생회비 정보가 없는 사용자는 verify가 false여야 한다")
    void refreshBorrowerCache_WithoutVerification() {
        // Given: 사용자만 있고 납부 정보는 없음
        String userId = "20249999";
        borrowerRepository.save(BorrowerDto.builder().id(userId).name("신규").build());

        // When
        cacheScheduledTask.refreshBorrowerCache();

        // Then
        CacheBorrowerDto cachedData = borrowerCache.getIfPresent(userId);
        assertThat(cachedData).isNotNull();
        assertThat(cachedData.isVerify()).isFalse(); // 기본값 검증
        assertThat(cachedData.getS3Link()).isNull();
    }
}