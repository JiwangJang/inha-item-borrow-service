package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class BorrowerRepositoryTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BorrowerRepository borrowerRepository;
    private BorrowerDto borrowerDto;

    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();
        borrowerDto = BorrowerDto.builder()
                .id("123")
                .password("123")
                .email("123")
                .name("123")
                .phonenumber("123")
                .studentNumber("123")
                .accountNumber("123")
                .refreshToken("123")
                .build();
        borrowerRepository.save(borrowerDto);
    }

    @DisplayName("id 찾기 성공")
    @Test
    void findById() {
        Borrower result = borrowerRepository.findById(borrowerDto.getId());
        assertThat(result.getAccountNumber()).isEqualTo(borrowerDto.getAccountNumber());
        assertThat(result.getEmail()).isEqualTo(borrowerDto.getEmail());

    }

    @DisplayName("id 찾기 실패")
    @Test
    void failedFindById() {
        String id = "asdf";
        assertThatThrownBy(() -> borrowerRepository.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND.getMessage());
    }

    @DisplayName("전체조회")
    @Test
    void findAll() {
        List<Borrower> result = borrowerRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @DisplayName("저장")
    @Test
    void save() {
        borrowerRepository.deleteAll();
        borrowerRepository.save(borrowerDto);
        borrowerDto.setId("test");
        borrowerRepository.save(borrowerDto);
        List<Borrower> result = borrowerRepository.findAll();
        assertThat(result).hasSize(2);
        Borrower result2 = borrowerRepository.findById("test");
        assertThat(result2.getEmail()).isEqualTo(borrowerDto.getEmail());
        assertThat(result2.getName()).isEqualTo(borrowerDto.getName());
    }

    @DisplayName("비밀번호 수정")
    @Test
    void patchPassword() {
        borrowerRepository.patchPassword("456", "123");
        Borrower result = borrowerRepository.findById("123");
        assertThat(passwordEncoder.matches("456", result.getPassword())).isTrue();
    }

    @Test
    void patchEmail() {
        borrowerRepository.patchEmail("456", "123");
        assertThat(borrowerRepository.findById("123").getEmail()).isEqualTo("456");
    }

    @Test
    void patchName() {
        borrowerRepository.patchName("456", "123");
        assertThat(borrowerRepository.findById("123").getName()).isEqualTo("456");
    }

    @Test
    void patchPhoneNumber() {
        borrowerRepository.patchPhoneNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getPhonenumber()).isEqualTo("456");
    }

    @Test
    void patchStudentNumber() {
        borrowerRepository.patchStudentNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getStudentNumber()).isEqualTo("456");
    }

    @Test
    void patchAccountNumber() {
        borrowerRepository.patchAccountNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getAccountNumber()).isEqualTo("456");
    }
}
