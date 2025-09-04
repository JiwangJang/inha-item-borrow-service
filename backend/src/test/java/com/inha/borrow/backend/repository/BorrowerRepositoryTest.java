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
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class BorrowerRepositoryTest {
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

    @DisplayName("id 찾기 (실패 조회값 없음)")
    @Test
    void FindByIdFailNotFoundId() {
        String id = "asdf";
        assertThatThrownBy(() -> borrowerRepository.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND.getMessage());
    }

    @DisplayName("전체조회 성공")
    @Test
    void findAll() {
        List<Borrower> result = borrowerRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @DisplayName("전체조회 (실패 조회값 없음)")
    @Test
    void findAllFailNotFoundId() {
        borrowerRepository.deleteAll();
        assertThatThrownBy(() -> borrowerRepository.findAll())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("등록된 대여자가 없습니다.");
    }

    @DisplayName("저장 성공")
    @Test
    void save() {
        borrowerRepository.deleteAll();
        borrowerRepository.save(borrowerDto);
        Borrower result2 = borrowerRepository.findById("123");
        assertThat(result2.getEmail()).isEqualTo(borrowerDto.getEmail());
        assertThat(result2.getName()).isEqualTo(borrowerDto.getName());
    }

    @DisplayName("비밀번호 수정 성공")
    @Test
    void patchPassword() {
        borrowerRepository.patchPassword("456", "123");
        Borrower result = borrowerRepository.findById("123");
        assertThat(result.getPassword()).isEqualTo("456");
    }

    @DisplayName("비밀번호 수정 (실패 id 미존재)")
    @Test
    void patchPasswordFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchPassword("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }

    @DisplayName("이메일 수정 성공")
    @Test
    void patchEmail() {
        borrowerRepository.patchEmail("456", "123");
        assertThat(borrowerRepository.findById("123").getEmail()).isEqualTo("456");
    }

    @DisplayName("이메일 수정 (실패 id 미존재)")
    @Test
    void patchEmailFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchEmail("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }

    @DisplayName("이름 수정 성공")
    @Test
    void patchName() {
        borrowerRepository.patchName("456", "123");
        assertThat(borrowerRepository.findById("123").getName()).isEqualTo("456");
    }

    @DisplayName("이름 수정 (실패 id 미존재)")
    @Test
    void patchNameFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchName("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }

    @DisplayName("전화번호 수정 성공")
    @Test
    void patchPhoneNumber() {
        borrowerRepository.patchPhoneNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getPhonenumber()).isEqualTo("456");
    }

    @DisplayName("전화번호 수정 (실패 id 미존재)")
    @Test
    void patchPhoneNumberFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchPhoneNumber("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }

    @DisplayName("학번 수정 성공")
    @Test
    void patchStudentNumber() {
        borrowerRepository.patchStudentNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getStudentNumber()).isEqualTo("456");
    }

    @DisplayName("학번 수정 (실패 id 미존재)")
    @Test
    void patchStudentFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchStudentNumber("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }

    @DisplayName("계좌번호 수정 성공")
    @Test
    void patchAccountNumber() {
        borrowerRepository.patchAccountNumber("456", "123");
        assertThat(borrowerRepository.findById("123").getAccountNumber()).isEqualTo("456");
    }

    @DisplayName("계좌번호 수정 (실패 id 미존재)")
    @Test
    void patchAccountNumberFailNotFoundId() {
        assertThatThrownBy(() -> borrowerRepository.patchAccountNumber("456", "321"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("요청하신 대여자가 존재하지 않습니다.");
    }
}
