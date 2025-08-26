package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.user.PatchPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class BorrowerServiceTest {

    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private BorrowerRepository borrowerRepository;

    private BorrowerDto borrowerDto;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();
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
        borrowerService.patchEmail("456","123");
        assertThat(borrowerService.findById("123").getEmail()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 이름 변경 성공")
    void patchName() {
        borrowerService.patchName("456","123");
        assertThat(borrowerService.findById("123").getName()).isEqualTo("456");
    }
    @Test
    @DisplayName("ID로 번호 변경 성공")
    void patchPhoneNumber() {
        borrowerService.patchPhoneNumber("456","123");
        assertThat(borrowerService.findById("123").getPhonenumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 학번 변경 성공")
    void patchStudentNumber() {
        borrowerService.patchStudentNumber("456","123");
        assertThat(borrowerService.findById("123").getStudentNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 계좌번호 변경 성공")
    void patchAccountNumber() {
        borrowerService.patchAccountNumber("456","123");
        assertThat(borrowerService.findById("123").getAccountNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("ID로 계정 상태 변경 성공")
    void patchWithDrawal() {
        borrowerService.patchWithDrawal(true,"123");
        assertThat(borrowerService.findById("123").isWithDrawal()).isTrue();
    }

    @Test
    @DisplayName("ID로 밴 상태 변경 성공")
    void patchBan() {
        borrowerService.patchBan(true,"123");
        assertThat(borrowerService.findById("123").isBan()).isTrue();
    }

    @Test
    @DisplayName("ID로 비밀번호 변경 성공")
    void patchPassword() {
        PatchPasswordDto patchPasswordDto = new PatchPasswordDto("Absssf1@2","Absssf1@2sss");
        borrowerService.patchPassword(patchPasswordDto,"123");
        Borrower result = borrowerService.findById("123");
        assertThat(passwordEncoder.matches(patchPasswordDto.getNewPassword(),result.getPassword())).isTrue();


    }

    @Test
    @DisplayName("ID로 비밀번호 변경 실패 비밀번호 검증 미통과 ")
    void patchPasswordFailMissMatchPassword() {
        PatchPasswordDto patchPasswordDto = new PatchPasswordDto("false","Absssf1@2sss");
        assertThatThrownBy(()->borrowerService.patchPassword(patchPasswordDto,"123"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("비밀번호가 다릅니다.");

    }

}
