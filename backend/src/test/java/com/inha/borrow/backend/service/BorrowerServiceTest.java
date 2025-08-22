package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.user.PatchPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
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

    @Test
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
    void patchEmail() {
        borrowerService.patchEmail("456","123");
        assertThat(borrowerService.findById("123").getEmail()).isEqualTo("456");
    }

    @Test
    void patchName() {
        borrowerService.patchName("456","123");
        assertThat(borrowerService.findById("123").getName()).isEqualTo("456");
    }

    @Test
    void patchPhoneNumber() {
        borrowerService.patchPhoneNumber("456","123");
        assertThat(borrowerService.findById("123").getPhonenumber()).isEqualTo("456");
    }

    @Test
    void patchStudentNumber() {
        borrowerService.patchStudentNumber("456","123");
        assertThat(borrowerService.findById("123").getStudentNumber()).isEqualTo("456");
    }

    @Test
    void patchAccountNumber() {
        borrowerService.patchAccountNumber("456","123");
        assertThat(borrowerService.findById("123").getAccountNumber()).isEqualTo("456");
    }

    @Test
    void patchWithDrawal() {
        borrowerService.patchWithDrawal(true,"123");
        assertThat(borrowerService.findById("123").isWithDrawal()).isTrue();
    }

    @Test
    void patchBan() {
        borrowerService.patchBan(true,"123");
        assertThat(borrowerService.findById("123").isBan()).isTrue();
    }

    @Test
    void patchPassword() {
        PatchPasswordDto patchPasswordDto = new PatchPasswordDto("123","456");
        borrowerService.patchPassword(patchPasswordDto,"123");
    }
}