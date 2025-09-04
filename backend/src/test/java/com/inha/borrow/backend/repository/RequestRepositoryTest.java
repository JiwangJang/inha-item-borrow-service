package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    private SaveRequestDto saveRequestDto;

    private int results;

    @BeforeEach
    void setUp(){
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        ItemDto itemDto = new ItemDto(
                "우산","3층","123",123
        );
        Item savedItem = itemRepository.save(itemDto);
        saveRequestDto = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId("123")
                .returnAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 5, 15, 0)))
                .borrowerAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 4, 15, 0)))
                .type(RequestType.BORROW)
                .build();
        results = requestRepository.saveAndReturnId(saveRequestDto);
    }

    @Test
    @DisplayName("저장 성공")
    void save() {
        requestRepository.deleteAll();
        SaveRequestDto result = requestRepository.save(saveRequestDto);
        assertThat(result.getBorrowerAt()).isEqualTo(saveRequestDto.getBorrowerAt());
    }

    @Test
    void findById() {
        Request result = requestRepository.findById(results);
        assertThat(result.getBorrowerId()).isEqualTo(saveRequestDto.getBorrowerId());
    }

    @Test
    void findByCondition() {
        List<Request> result = requestRepository.findByCondition("123",RequestType.BORROW.name(), RequestState.PENDING.name());
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findAll() {
        List<Request> result = requestRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void patchRequest() {
        LocalDateTime fixedTime = LocalDateTime.of(2025, 8, 31, 17, 22, 0);
        Timestamp borrowerAt = Timestamp.valueOf(fixedTime);
        Timestamp returnAt = Timestamp.valueOf(fixedTime.plusDays(3));
        PatchRequestDto patchRequestDto = PatchRequestDto.builder()
                .borrowerAt(borrowerAt)
                .returnAt(returnAt)
                .type(RequestType.BORROW)
                .build();
        requestRepository.patchRequest(patchRequestDto,results,"123");
        Request result = requestRepository.findById(results);
        assertThat(result.getBorrowerAt()).isEqualTo(borrowerAt);
    }

    @Test
    void cancelRequest() {
        requestRepository.cancelRequest(results,"123");
        Request result = requestRepository.findById(results);
        assertThat(result.getCancel()).isTrue();
    }

    @Test
    void evaluationRequest() {
        requestRepository.evaluationRequest(RequestState.ASSIGNED,results);
        Request result = requestRepository.findById(results);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
    }
}