package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto; // DTO import 확인
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BorrowerAgreementRepositoryTest {

    @Autowired
    private BorrowerAgreementRepository borrowerAgreementRepository;

    @Autowired
    private BorrowerRepository borrowerRepository; // [추가] 부모 데이터 생성을 위해 주입

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String TEST_BORROWER_ID = "20241234";
    private final String TEST_VERSION = "v1";

    @BeforeEach
    void setUp() {
        // 1. 자식 테이블(동의 내역) 데이터 삭제 (순서 중요: 자식 -> 부모)
        jdbcTemplate.update("DELETE FROM borrower_privacy_agreement");

        // 2. 부모 테이블(대출자) 데이터 삭제 (테스트 간 충돌 방지)
        // 주의: borrowerRepository.deleteAll()이 없다면 jdbcTemplate으로 삭제
        jdbcTemplate.update("DELETE FROM borrower WHERE id = ?", TEST_BORROWER_ID);

        // 3. [핵심] 부모 데이터(Borrower) 먼저 생성
        BorrowerDto borrower = BorrowerDto.builder()
                .id(TEST_BORROWER_ID)
                .name("테스트유저")
                .department("컴퓨터공학과") // DTO 오타가 있다면 departemnt로 작성
                .phonenumber("010-1234-5678")
                .accountNumber("110-123-456789")
                .build();

        borrowerRepository.save(borrower);

        // 4. 자식 데이터(동의 내역) 저장 (이제 FK 오류가 발생하지 않음)
        borrowerAgreementRepository.saveAgreement(TEST_BORROWER_ID, TEST_VERSION);
    }

    @DisplayName("borrowerId로 찾기 성공")
    @Test
    void findByBorrowerId() {
        List<BorrowerAgreement> result = borrowerAgreementRepository.findByBorrowerId(TEST_BORROWER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getFirst().getBorrowerId()).isEqualTo(TEST_BORROWER_ID);
    }

    @DisplayName("borrowerId로 찾기 (실패 조회값 없음)")
    @Test
    void findByBorrowerIdFailNotFoundId() {
        String invalidId = "unknown_id";

        assertThatThrownBy(() -> borrowerAgreementRepository.findByBorrowerId(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_AGREEMENT.getMessage());
    }

    @DisplayName("전체조회 성공")
    @Test
    void findAllAgreement() {
        // 추가 데이터를 넣으려면 그에 맞는 부모(Borrower)도 있어야 함
        String secondUserId = "another";

        // 두 번째 유저 생성
        borrowerRepository.save(BorrowerDto.builder()
                .id(secondUserId)
                .name("유저2")
                .department("기계공학과")
                .phonenumber("010-9876-5432")
                .accountNumber("333-444-555555")
                .build());

        borrowerAgreementRepository.saveAgreement(secondUserId, "v1");

        List<BorrowerAgreement> result = borrowerAgreementRepository.findAllAgreement();

        assertThat(result.size()).isEqualTo(2);
    }

    @DisplayName("전체조회 (데이터 없음)")
    @Test
    void findAllAgreementEmpty() {
        jdbcTemplate.update("DELETE FROM borrower_privacy_agreement");

        List<BorrowerAgreement> result = borrowerAgreementRepository.findAllAgreement();

        assertThat(result).isEmpty();
    }

    @DisplayName("버전으로 조회 성공")
    @Test
    void findByVersion() {
        // 테스트를 위해 추가 유저들이 필요함
        createDummyBorrower("user2");
        createDummyBorrower("user3");

        borrowerAgreementRepository.saveAgreement("user2", "v2");
        borrowerAgreementRepository.saveAgreement("user3", TEST_VERSION);

        List<BorrowerAgreement> result = borrowerAgreementRepository.findbyVersion(TEST_VERSION);

        // setUp(1개) + user3(1개) = 총 2개
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("version").containsOnly(TEST_VERSION);
    }

    @DisplayName("저장 성공")
    @Test
    void saveAgreement() {
        // 기존 setUp 데이터 삭제
        jdbcTemplate.update("DELETE FROM borrower_privacy_agreement");

        String newBorrowerId = "new_user";
        String newVersion = "v3";

        // [중요] 저장 테스트를 할 때도 부모 유저가 있어야 함
        createDummyBorrower(newBorrowerId);

        int generatedId = borrowerAgreementRepository.saveAgreement(newBorrowerId, newVersion);

        List<BorrowerAgreement> result = borrowerAgreementRepository.findByBorrowerId(newBorrowerId);

        assertThat(generatedId).isGreaterThan(0);
        assertThat(result.getFirst().getBorrowerId()).isEqualTo(newBorrowerId);
        assertThat(result.getFirst().getVersion()).isEqualTo(newVersion);
    }

    @DisplayName("borrowerId로 찾기 실패 (존재하지 않는 ID 조회 시 예외 발생)")
    @Test
    void findByBorrowerId_Fail_NotFound() {
        // given
        // DB에 절대 없을 법한 ID를 지정합니다.
        // (이전 에러를 고려해 컬럼 길이 제한인 8글자 이내로 설정)
        String nonExistentId = "unknown";

        // when & then
        assertThatThrownBy(() -> borrowerAgreementRepository.findByBorrowerId(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class) // 1. 예외 클래스 타입 확인
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_AGREEMENT.getMessage()); // 2. 에러 메시지 확인
    }

    // 도우미 메서드: 유저 생성 반복을 줄이기 위함
    private void createDummyBorrower(String id) {
        borrowerRepository.save(BorrowerDto.builder()
                .id(id)
                .name("Dummy")
                .department("Dept")
                .phonenumber("010-0000-0000")
                .accountNumber("000-000-000000")
                .build());
    }
}