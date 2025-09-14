package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import({RequestRepository.class,ItemRepository.class,BorrowerRepository.class})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
class RequestRepositoryTest {

    private RequestRepository requestRepository;
    private ItemRepository itemRepository;
    private BorrowerRepository borrowerRepository;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    public RequestRepositoryTest(RequestRepository requestRepository,ItemRepository itemRepository,BorrowerRepository borrowerRepository, JdbcTemplate jdbcTemplate) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.borrowerRepository = borrowerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    private SaveRequestDto saveRequestDto;
    private ItemDto itemDto;
    private BorrowerDto borrowerDto;



    @BeforeEach
    void setUp(){
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        borrowerRepository.deleteAll();

        itemDto = new ItemDto();
        itemDto.setName("123");
        itemDto.setPassword("123");
        itemDto.setLocation("123");
        itemDto.setPrice(123);

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

        Item itemId = itemRepository.save(itemDto);
        borrowerRepository.save(borrowerDto);
        saveRequestDto = new SaveRequestDto(
                itemId.getId(),
                borrowerDto.getId(),
                Timestamp.valueOf(LocalDateTime.now().plusDays(7)),
                Timestamp.valueOf(LocalDateTime.now().plusDays(7)),
                RequestType.BORROW
        );
        requestRepository.save(saveRequestDto);
    }

    @Test
    @DisplayName("리퀘스트 저장 성공")
    void save() {
        requestRepository.deleteAll();
        Request result = requestRepository.findById(borrowerDto.getId(),requestRepository.save(saveRequestDto));
        assertThat(result.getBorrowerId()).isEqualTo(saveRequestDto.getBorrowerId());
    }

    @Test
    @DisplayName("리퀘스트 단건 조회 성공")
    void findById() {
        requestRepository.deleteAll();
        Request result = requestRepository.findById(borrowerDto.getId(),requestRepository.save(saveRequestDto));
        assertThat(result.getBorrowerId()).isEqualTo(borrowerDto.getId());
    }

    @Test
    @DisplayName("리퀘스트 단건 조회 (실패 BorrowerId 미존재)")
    void findByIdFailNotFoundBorrowerId() {
        requestRepository.deleteAll();
        assertThatThrownBy(()-> requestRepository.findById("321",requestRepository.save(saveRequestDto)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());
    }

    @Test
    @DisplayName("리퀘스트 조건 조회 성공")
    void findByCondition() {
        List<Request> result = requestRepository.findByCondition("123","BORROW","PENDING");
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("리퀘스트 조건 조회 (실패 BorrowerId 미존재)")
    void findByConditionFailNotFoundBorrowerId() {
        assertThatThrownBy(()->requestRepository.findByCondition("321","BORROW","PENDING"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());

    }

    @Test
    @DisplayName("리퀘스트 전체 조회 성공")
    void findAll() {
        List<Request> result = requestRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("리퀘스트 전체 조회 (실패 조회값 없음)")
    void findAllFail() {
        requestRepository.deleteAll();
        assertThatThrownBy(()->requestRepository.findAll())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());

    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchRequest() {
        PatchRequestDto patchRequestDto =new PatchRequestDto();
        patchRequestDto.setBorrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        patchRequestDto.setReturnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(8)));
        int requestId = requestRepository.save(saveRequestDto);
        requestRepository.patchRequest(patchRequestDto,requestId ,borrowerDto.getId());
        Request result = requestRepository.findById(borrowerDto.getId(),requestId);
        assertThat(result.getType()).isEqualTo(RequestType.BORROW);
    }

    @Test
    @DisplayName("리퀘스트 수정 (실패 borrowerId 미존재")
    void patchRequestFailNotFoundBorrowerId() {
        PatchRequestDto patchRequestDto =new PatchRequestDto();
        patchRequestDto.setBorrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        patchRequestDto.setReturnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(8)));
        assertThatThrownBy(()->requestRepository.patchRequest(patchRequestDto,1 ,borrowerDto.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());
    }

    @Test
    @DisplayName("리퀘스트 취소 성공")
    void cancelRequest() {
        int requestId = requestRepository.save(saveRequestDto);
        requestRepository.cancelRequest(requestId,borrowerDto.getId());
        Request result = requestRepository.findById(borrowerDto.getId(),requestId);
        assertThat(result.getCancel()).isTrue();
    }

    @Test
    @DisplayName("리퀘스트 취소 (실패 borrowerId 미존재")
    void cancelRequestFailNotFoundBorrowerId() {
        assertThatThrownBy(()->requestRepository.cancelRequest(3,borrowerDto.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());
    }

    @Test
    @DisplayName("리퀘스트 평가 성공")
    void evaluationRequest() {
        int requestId = requestRepository.save(saveRequestDto);
        requestRepository.evaluationRequest(RequestState.ASSIGNED,requestId);
        Request result = requestRepository.findById(borrowerDto.getId(),requestId);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
    }

    @Test
    @DisplayName("리퀘스트 평가 (실패 borrowerId 미존재")
    void evaluationRequestFailNotFoundBorrowerId() {
        assertThatThrownBy(()->requestRepository.evaluationRequest(RequestState.ASSIGNED,3))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());

    }

    @Test
    @DisplayName("리퀘스트 삭제 성공")
    void deleteRequest() {
        requestRepository.deleteAll();
        int requestId = requestRepository.save(saveRequestDto);
        requestRepository.deleteRequest(requestId);
        assertThatThrownBy(()->requestRepository.findAll())
                .isInstanceOf(ResourceNotFoundException.class);

    }
    @Test
    @DisplayName("리퀘스트 삭제 (실패 borrowerId 미존재")
    void deleteRequestFailNotFoundBorrowerId() {
        assertThatThrownBy(()->requestRepository.deleteRequest(3))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND_REQUEST.getMessage());

    }
}