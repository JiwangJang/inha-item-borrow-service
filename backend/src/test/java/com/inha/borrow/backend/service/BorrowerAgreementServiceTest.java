package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.TempBorrowerInfoDto;
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.repository.BorrowerAgreementRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerAgreementServiceTest {

    @Mock
    private BorrowerAgreementRepository borrowerAgreementRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private Cache<String, TempBorrowerInfoDto> tempBorrowerCache;

    @InjectMocks
    private BorrowerAgreementService borrowerAgreementService;

    @Test
    @DisplayName("saveAgreement: 캐시 데이터를 사용하여 대출자 정보와 동의 내역을 성공적으로 저장한다")
    void saveAgreement_success() {
        // given
        String borrowerId = "20240001";

        // [문제 해결 포인트]
        // 기존: new AgreementDto("010-1234-5678", "110-123-456789", "v1.1");
        // 실제 AgreementDto의 생성자 순서가 (..., phoneNumber, accountNumber) 형태로 추정됩니다.
        // DTO 내부를 확인하여 올바른 순서로 넣어주거나, 아래처럼 Builder를 사용하세요.
        AgreementDto agreementDto = AgreementDto.builder()
                .phoneNumber("010-1234-5678")
                .accountNumber("110-123-456789")
                .version("v1.1")
                .build();

        TempBorrowerInfoDto cachedInfo = new TempBorrowerInfoDto("홍길동", "컴퓨터공학과");

        when(tempBorrowerCache.getIfPresent(borrowerId)).thenReturn(cachedInfo);
        when(borrowerAgreementRepository.saveAgreement(borrowerId, agreementDto.getVersion())).thenReturn(1);

        // when
        int resultId = borrowerAgreementService.saveAgreement(borrowerId, agreementDto);

        // then
        assertEquals(1, resultId);

        // verify
        verify(borrowerRepository, times(1)).save(argThat(dto -> {
            // 디버깅을 위해 출력해보셔도 좋습니다
            // System.out.println("Actual Phone: " + dto.getPhonenumber());

            return dto.getId().equals(borrowerId) &&
                    dto.getName().equals("홍길동") &&
                    dto.getPhonenumber().equals("010-1234-5678"); // 이제 일치할 것입니다.
        }));

        verify(borrowerAgreementRepository, times(1)).saveAgreement(borrowerId, "v1.1");
    }

    @Test
    @DisplayName("findAllAgreement: 모든 동의 내역 리스트를 조회한다")
    void findAllAgreement_success() {
        // given
        List<BorrowerAgreement> expectedAgreements = List.of(
                new BorrowerAgreement(1, "user1", LocalDateTime.now(), "v1"),
                new BorrowerAgreement(2, "user2", LocalDateTime.now(), "v1")
        );
        when(borrowerAgreementRepository.findAllAgreement()).thenReturn(expectedAgreements);

        // when
        List<BorrowerAgreement> result = borrowerAgreementService.findAllAgreement();

        // then
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getBorrowerId());
        verify(borrowerAgreementRepository, times(1)).findAllAgreement();
    }

    @Test
    @DisplayName("findByVersion: 특정 버전의 동의 내역 리스트를 조회한다")
    void findByVersion_success() {
        // given
        String version = "v1.2";
        List<BorrowerAgreement> expectedAgreements = List.of(
                new BorrowerAgreement(1, "user1", LocalDateTime.now(), version)
        );
        when(borrowerAgreementRepository.findbyVersion(version)).thenReturn(expectedAgreements);

        // when
        List<BorrowerAgreement> result = borrowerAgreementService.findByVersion(version);

        // then
        assertEquals(1, result.size());
        assertEquals(version, result.get(0).getVersion());
        verify(borrowerAgreementRepository, times(1)).findbyVersion(version);
    }

    @Test
    @DisplayName("findByBorrower: 사용자 ID로 동의 내역 리스트를 조회한다")
    void findByBorrower_success() {
        // given
        String borrowerId = "user123";
        // 단일 객체 대신 List로 감싸서 준비
        List<BorrowerAgreement> expectedList = List.of(
                new BorrowerAgreement(1, borrowerId, LocalDateTime.now(), "v1")
        );

        // Repository가 List를 반환하도록 설정
        when(borrowerAgreementRepository.findByBorrowerId(borrowerId)).thenReturn(expectedList);

        // when
        // 결과값 타입도 List로 변경
        List<BorrowerAgreement> result = borrowerAgreementService.findByBorrower(borrowerId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size()); // 리스트 크기 확인
        assertEquals(borrowerId, result.get(0).getBorrowerId()); // 0번째 요소의 ID 확인

        verify(borrowerAgreementRepository, times(1)).findByBorrowerId(borrowerId);
    }
}