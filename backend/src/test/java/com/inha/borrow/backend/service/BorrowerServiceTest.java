package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BorrowerServiceTest {

    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private BorrowerRepository borrowerRepository;

    private BorrowerDto borrowerDto;

    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();
        borrowerDto = BorrowerDto.builder()
                .id("123")
                .name("123")
                .phonenumber("123")
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
}
