package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RequestServiceTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    BorrowerRepository borrowerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private int requestId;
    private Item savedItem;
    private BorrowerDto borrowerDto;
    private SaveRequestDto saveRequestDto;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        borrowerRepository.deleteAll();
        itemRepository.deleteAll();
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

        ItemDto itemDto = new ItemDto("우산", "3층", "123", 123);
        savedItem = itemRepository.save(itemDto);

        saveRequestDto = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId("123")
                .borrowerAt(Timestamp.valueOf(LocalDateTime.of(2025, 8, 31, 17, 22, 0)))
                .returnAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 3, 17, 22, 0)))
                .type(RequestType.BORROW)
                .build();

        requestId = requestRepository.saveAndReturnId(saveRequestDto);
    }

    @Test
    @DisplayName("리퀘스트 저장 성공")
    void saveRequest() {
        SaveRequestDto result = requestService.saveRequest(saveRequestDto, savedItem.getId());

        // 🔽 5. 검증
        assertThat(result).isNotNull();
        assertThat(result.getBorrowerId()).isEqualTo(borrowerDto.getId());
    }

    @Test
    @DisplayName("리퀘스트 조회 성공")
    void findById() {
        Request result = requestService.findById(requestId);
        assertThat(result.getBorrowerId()).isEqualTo("123");
    }

    @Test
    @DisplayName("조건 조회 성공")
    void findByCondition() {
        List<Request> result = requestService.findByCondition("123", "BORROW", "PENDING");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("전체 조회 성공")
    void findAll() {
        List<Request> result = requestService.findAll();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("사용자 리퀘스트 조회 성공")
    void findRequestUser() {
        List<Request> result = requestService.findRequestUser("123");
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchRequest() {
        PatchRequestDto dto = PatchRequestDto.builder()
                .borrowerAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 1, 10, 0)))
                .returnAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 4, 10, 0)))
                .type(RequestType.BORROW)
                .build();

        requestService.patchRequest(dto, requestId, "123");

        Request result = requestService.findById(requestId);
        assertThat(result.getBorrowerAt().toLocalDateTime().withNano(0)).isEqualTo(dto.getBorrowerAt().toLocalDateTime().withNano(0));
    }

    @Test
    @DisplayName("리퀘스트 취소 성공")
    void cancelRequest() {
        requestService.cancelRequest(requestId, "123");
        Request result = requestService.findById(requestId);
        assertThat(result.getCancel()).isTrue();
    }

    @Test
    @DisplayName("리퀘스트 상태 평가 성공")
    void evaluationRequest() {
        requestService.evaluationRequest(RequestState.ASSIGNED, requestId);
        Request result = requestService.findById(requestId);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
    }
}
