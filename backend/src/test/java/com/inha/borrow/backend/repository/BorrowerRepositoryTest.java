package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
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
class BorrowerRepositoryTest {
    @Autowired
    private BorrowerRepository borrowerRepository;
    @Autowired
    private RequestRepository requestRepository;
    private BorrowerDto borrowerDto;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
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

    @DisplayName("id 찾기 성공")
    @Test
    void findById() {
        Borrower result = borrowerRepository.findById(borrowerDto.getId());
        assertThat(result.getAccountNumber()).isEqualTo(borrowerDto.getAccountNumber());
    }

    @DisplayName("id 찾기 (실패 조회값 없음)")
    @Test
    void FindByIdFailNotFoundId() {
        String id = "asdf";
        assertThatThrownBy(() -> borrowerRepository.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_BORROWER.getMessage());
    }

    @DisplayName("전체조회 성공")
    @Test
    void findAll() {
        List<Borrower> result = borrowerRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @DisplayName("저장 성공")
    @Test
    void save() {
        borrowerRepository.deleteAll();
        borrowerRepository.save(borrowerDto);
        Borrower result2 = borrowerRepository.findById("123");
        assertThat(result2.getName()).isEqualTo(borrowerDto.getName());
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
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_BORROWER.getMessage());
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
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_BORROWER.getMessage());
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
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_BORROWER.getMessage());
    }

    @DisplayName("전화번호 및 계좌번호 저장 성공")
    @Test
    void savePhoneAccountNumber() {
        // given (기존 setUp에서 "123" 아이디가 생성되어 있다고 가정)
        SavePhoneAccountNumberDto dto = new SavePhoneAccountNumberDto("010-1234-5678", "110-123-456789");

        // when
        borrowerRepository.savePhoneAccountNumber("123", dto);

        // then
        // 엔티티 구조에 따라 getPhoneNumber() 또는 getPhonenumber()로 맞춰서 사용하세요.
        assertThat(borrowerRepository.findById("123").getPhonenumber()).isEqualTo("010-1234-5678");
        assertThat(borrowerRepository.findById("123").getAccountNumber()).isEqualTo("110-123-456789");
    }

    @DisplayName("전화번호 및 계좌번호 저장 (실패 id 미존재)")
    @Test
    void savePhoneAccountNumberFailNotFoundId() {
        // given
        SavePhoneAccountNumberDto dto = new SavePhoneAccountNumberDto("010-1234-5678", "110-123-456789");

        // when & then ("321"은 존재하지 않는 ID로 가정)
        assertThatThrownBy(() -> borrowerRepository.savePhoneAccountNumber("321", dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_BORROWER.getMessage());
    }
}
