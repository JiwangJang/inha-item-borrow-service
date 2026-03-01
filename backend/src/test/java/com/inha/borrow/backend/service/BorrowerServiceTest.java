package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BorrowerServiceTest {

    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private BorrowerRepository borrowerRepository;
    @Autowired
    private StudentCouncilFeeVerificationRepository studentCouncilFeeVerificationRepository;


    private BorrowerDto borrowerDto;

    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();
        borrowerDto = BorrowerDto.builder()
                .id("123")
                .name("123")
                .phonenumber("123")
                .department("123")
                .accountNumber("123")
                .build();
        borrowerRepository.save(borrowerDto);
    }

    @Test
    @DisplayName("id로 찾기 성공")
    void findById() {
        Borrower result = borrowerService.findById("123");
        assertThat(result.getAccountNumber()).isEqualTo(borrowerDto.getAccountNumber());
    }

    @Test
    void findAll() {
        List<Borrower> result = borrowerService.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("ID로 이름 변경 성공")
    void patchName() {
        borrowerService.patchName("456", "123");
        assertThat(borrowerService.findById("123").getName()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 계좌번호 변경 성공")
    void patchAccountNumber() {
        borrowerService.patchAccountNumber("456", "123");
        assertThat(borrowerService.findById("123").getAccountNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 밴 상태 변경 성공")
    void patchBan() {
        borrowerService.patchBan(true, "123");
        assertThat(borrowerService.findById("123").isBan()).isTrue();
    }
    @Test
    @DisplayName("전화번호 및 계좌번호 저장 성공 (학생회비 인증 완료)")
    void savePhoneAccountNumber_Success() {
        // given
        String testId = "123";
        SavePhoneAccountNumberDto updateDto = new SavePhoneAccountNumberDto("123", "123");

        studentCouncilFeeVerificationRepository.initialSave(testId);
        studentCouncilFeeVerificationRepository.updateForAdmin(testId,true,"승인");

        // when
        borrowerService.savePhoneAccountNumber(testId, updateDto);

        // then
        // 실제 DB에 잘 업데이트되었는지 id로 다시 조회하여 검증
        Borrower result = borrowerService.findById(testId);
        assertThat(result.getPhonenumber()).isEqualTo("123");
        assertThat(result.getAccountNumber()).isEqualTo("123");
    }

    @Test
    @DisplayName("전화번호 및 계좌번호 저장 실패 (학생회비 미인증 시 예외 발생)")
    void savePhoneAccountNumber_Fail_NotVerified() {
        // given
        String testId = "123";
        SavePhoneAccountNumberDto updateDto = new SavePhoneAccountNumberDto("010-9999-9999", "999-999-999999");

        studentCouncilFeeVerificationRepository.initialSave(testId);
        studentCouncilFeeVerificationRepository.updateForAdmin(testId,false,"승인");

        // when & then
        // AccessDeniedException이 발생하고, 에러 메시지에 NOT_ALLOWED_COUNCIL_FEE가 포함되어 있는지 검증
        assertThatThrownBy(() -> borrowerService.savePhoneAccountNumber(testId, updateDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("NOT_ALLOWED_COUNCIL_FEE");
    }

}
