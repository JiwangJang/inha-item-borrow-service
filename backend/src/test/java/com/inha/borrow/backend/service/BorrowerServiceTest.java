package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.SMSCodeCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.PatchPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;
import com.inha.borrow.backend.model.entity.SMSCode;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BorrowerServiceTest {

    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private BorrowerRepository borrowerRepository;
    @Autowired
    private SMSCodeCache smsCodeCache;

    private BorrowerDto borrowerDto;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();
        smsCodeCache.deleteAll();
        borrowerDto = BorrowerDto.builder()
                .id("123")
                .password(passwordEncoder.encode("Absssf1@2"))
                .email("123")
                .name("123")
                .phonenumber("123")
                .studentNumber("123")
                .accountNumber("123")
                .refreshToken("123")
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
    @DisplayName("ID로 이메일 변경 성공")
    void patchEmail() {
        borrowerService.patchEmail("456", "123");
        assertThat(borrowerService.findById("123").getEmail()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 이름 변경 성공")
    void patchName() {
        borrowerService.patchName("456", "123");
        assertThat(borrowerService.findById("123").getName()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 번호 변경 성공 - 인증코드 일치")
    void patchPhoneNumberSuccess() {
        // given
        smsCodeCache.set("123", new SMSCode("654321"));
        PatchPhonenumberDto dto = new PatchPhonenumberDto("456", "654321");
        // when
        borrowerService.patchPhoneNumber("123", dto);
        // then
        assertThat(borrowerService.findById("123").getPhonenumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 번호 변경 실패 - 인증코드 불일치")
    void patchPhoneNumberFailWrongCode() {
        // given
        smsCodeCache.set("123", new SMSCode("111111"));
        PatchPhonenumberDto dto = new PatchPhonenumberDto("456", "222222");
        // when/then
        assertThatThrownBy(() -> borrowerService.patchPhoneNumber("123", dto))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("ID로 번호 변경 실패 - 인증코드 미발급")
    void patchPhoneNumberFailNoCode() {
        PatchPhonenumberDto dto = new PatchPhonenumberDto("456", "123456");
        assertThatThrownBy(() -> borrowerService.patchPhoneNumber("123", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("ID로 학번 변경 성공")
    void patchStudentNumber() {
        borrowerService.patchStudentNumber("456", "123");
        assertThat(borrowerService.findById("123").getStudentNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 계좌번호 변경 성공")
    void patchAccountNumber() {
        borrowerService.patchAccountNumber("456", "123");
        assertThat(borrowerService.findById("123").getAccountNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 계정 상태 변경 성공")
    void patchWithDrawal() {
        borrowerService.patchWithDrawal(true, "123");
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            borrowerService.findById("123").isWithDrawal();
        });

        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND_BORROWER.name());
        assertThat(ex.getMessage()).isEqualTo("존재하지 않는 대여자입니다.");

    }

    @Test
    @DisplayName("ID로 밴 상태 변경 성공")
    void patchBan() {
        borrowerService.patchBan(true, "123");
        assertThat(borrowerService.findById("123").isBan()).isTrue();
    }

    @Test
    @DisplayName("ID로 비밀번호 변경 성공")
    void patchPassword() {
        PatchPasswordDto patchPasswordDto = new PatchPasswordDto("Absssf1@2", "Absssf1@2sss");
        borrowerService.patchPassword(patchPasswordDto, "123");
        Borrower result = borrowerService.findById("123");
        assertThat(passwordEncoder.matches(patchPasswordDto.getNewPassword(), result.getPassword())).isTrue();

    }

    @Test
    @DisplayName("ID로 비밀번호 변경 실패 비밀번호 검증 미통과 ")
    void patchPasswordFailMissMatchPassword() {
        PatchPasswordDto patchPasswordDto = new PatchPasswordDto("false", "Absssf1@2sss");
        InvalidValueException ex = assertThrows(InvalidValueException.class, () -> {
            borrowerService.patchPassword(patchPasswordDto, "123");
        });

        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.name());
        assertThat(ex.getMessage()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("SMS 코드 발급 성공: 캐시에 저장 및 TTL 부여")
    void createSmsCodeStoresCodeWithTtl() {
        // given
        String borrowerId = "borrower001";
        String newPhone = "010-1234-5678";

        // when
        borrowerService.createSmsCode(borrowerId, newPhone);

        // then
        SMSCode saved = smsCodeCache.get(borrowerId);
        assertThat(saved.getCode()).isEqualTo("123456");
        assertThat(saved.getTtl()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("SMS 코드 재발급 시 기존 코드 덮어쓰기")
    void createSmsCodeOverwritesExisting() {
        // given
        String borrowerId = "borrower002";
        smsCodeCache.set(borrowerId, new SMSCode("000000"));

        // when
        borrowerService.createSmsCode(borrowerId, "010-0000-0000");

        // then
        SMSCode saved = smsCodeCache.get(borrowerId);
        assertThat(saved.getCode()).isEqualTo("123456");
    }

    // 테스트 추가: 만료된 코드 조회 시 예외 발생
    @Test
    @DisplayName("만료된 SMS 코드 조회 시 InvalidValueException")
    void expiredSmsCodeThrows() {
        String borrowerId = "expiredUser";
        // expired ttl
        smsCodeCache.setForTest(borrowerId, new SMSCode("999999", System.currentTimeMillis() - 1));

        InvalidValueException ex = assertThrows(InvalidValueException.class, () -> {
            smsCodeCache.get(borrowerId);
        });
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.SMS_CODE_EXPIRED.name());
    }

    // 테스트 추가: 만료된 코드 정리 후 조회 시 NOT_FOUND
    @Test
    @DisplayName("만료 코드 정리 후 조회 시 ResourceNotFoundException")
    void removeOldSmsCodeAndGet() {
        String borrowerId = "expiredUser2";
        smsCodeCache.setForTest(borrowerId, new SMSCode("999999", System.currentTimeMillis() - 1));
        smsCodeCache.removeOldSMSCode();

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> smsCodeCache.get(borrowerId));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
    }

}
