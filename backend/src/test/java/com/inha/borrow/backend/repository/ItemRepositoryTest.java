package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.BorrowedItemDto;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({ ItemRepository.class, RequestRepository.class, BorrowerRepository.class })
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    Item savedItem;

    String requester = "test_borrower";

    @BeforeEach
    void setUp() {
        BorrowerDto borrowerDto = new BorrowerDto(
                requester,
                "테스트용 이름",
                "010-0000-0000",
                "23123");
        borrowerRepository.save(borrowerDto);
        ItemDto item = new ItemDto();
        item.setName("우산");
        item.setLocation("어딘가");
        item.setPassword("1111");
        item.setPrice(1000);
        savedItem = itemRepository.save(item);

        SaveRequestDto saveRequest = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId(requester)
                .returnAt(Timestamp.from(Instant.now()))
                .borrowerAt(Timestamp.from(Instant.now()))
                .type(RequestType.BORROW)
                .build();
        int requestId = requestRepository.save(saveRequest).getRequestId();
        requestRepository.updateRequestState(RequestState.PERMIT, requestId);
    }

    @Test
    @DisplayName("아이템 업데이트 객체 유효성 검사")
    public void ItemReviseRequestDtoValidationTest() {
        // given
        ItemReviseRequestDto itemReviseRequestDto = new ItemReviseRequestDto();
        itemReviseRequestDto.setName("");
        itemReviseRequestDto.setLocation("");
        itemReviseRequestDto.setDeleteReason("");
        itemReviseRequestDto.setPassword("");
        itemReviseRequestDto.setState(ItemState.AFFORD);
        // when
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemReviseRequestDto>> constraintViolations = validator.validate(itemReviseRequestDto);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("대여물품 이름은 비울수 없습니다.");
    }

    @Test
    @DisplayName("아이템 객체 유효성 검사테스트")
    public void itemDtoValidationTest() {
        // given
        ItemDto item = new ItemDto();
        item.setLocation("");
        item.setName("");
        item.setPassword("");
        item.setPrice(0);
        // when
        // then
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemDto>> constraintViolations = validator.validate(item);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("대여물품 이름은 비울수 없습니다.");
    }

    @Test
    @DisplayName("아이템 삭제 유효성 검사")
    public void itemDeleteRequestDtoValidationTest() {
        // given
        ItemDeleteRequestDto dto = new ItemDeleteRequestDto();
        dto.setDeleteReason("");
        // when
        // then
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemDeleteRequestDto>> constraintViolations = validator.validate(dto);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("삭제이유는 비워둘 수 없습니다.");
    }

    @Test
    @DisplayName("아이템 저장")
    public void itemSaveSuccessTest() {
        // given
        ItemDto given = new ItemDto("name", "location", "password", 1233);
        // when
        Item savedItem = itemRepository.save(given);
        BorrowedItemDto borrowed = itemRepository.findById(savedItem.getId());
        // then
        assertThat(borrowed.getItemId()).isEqualTo(savedItem.getId());
    }

    @Test
    @DisplayName("전체 조회(관리자용)")
    public void findAllForAdminTest() {
        // given
        // when
        List<Item> result = itemRepository.findAllForAdmin();
        Item item = result.get(0);
        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(item.getId()).isEqualTo(savedItem.getId());
        assertThat(item.getPassword()).isEqualTo(savedItem.getPassword());
        assertThat(item.getLocation()).isEqualTo(savedItem.getLocation());
    }

    @Test
    @DisplayName("전체 조회(비 관리자용)")
    public void findAllForNotAdminTest() {
        // given
        // when
        List<Item> result = itemRepository.findAllForNotAdmin();
        Item item = result.get(0);
        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(item.getId()).isEqualTo(savedItem.getId());
        assertThat(item.getPassword()).isEqualTo(null);
        assertThat(item.getLocation()).isEqualTo(null);
    }

    @Test
    @DisplayName("아이디로 찾기(성공)")
    public void findByIdSuccessTest() {
        // given
        // when
        BorrowedItemDto borrowedItemDto = itemRepository.findById(savedItem.getId());
        // then
        assertThat(borrowedItemDto.getItemId()).isEqualTo(savedItem.getId());
    }

    @Test
    @DisplayName("여러 요청들이 한 아이템에 중복으로 돼있는 상황에서 아이디로 찾기테스트(PERMIT은 하나)")
    public void findByIdSuccessTestInSeveralRequests() {
        // given
        // 두명의 대여자준비
        BorrowerDto borrower1 = new BorrowerDto(
                "test_borrower1",
                "테스트용 이름",
                "010-0000-0000",
                "23123");
        BorrowerDto borrower2 = new BorrowerDto(
                "test_borrower2",
                "테스트용 이름",
                "010-0000-0000",
                "23123");

        // 두개의 요청 준비
        SaveRequestDto saveRequest1 = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId("test_borrower1")
                .returnAt(Timestamp.from(Instant.now()))
                .borrowerAt(Timestamp.from(Instant.now()))
                .type(RequestType.BORROW)
                .build();

        SaveRequestDto saveRequest2 = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId("test_borrower2")
                .returnAt(Timestamp.from(Instant.now()))
                .borrowerAt(Timestamp.from(Instant.now()))
                .type(RequestType.BORROW)
                .build();

        // when
        borrowerRepository.save(borrower1);
        borrowerRepository.save(borrower2);

        int id1 = requestRepository.save(saveRequest1).getRequestId();
        int id2 = requestRepository.save(saveRequest2).getRequestId();

        // 동일한 아이템에 여러 요청이 있을 경우 테스트
        // 첫번째 요청은 빌리고 반납까지 완료됐다고 가정
        requestRepository.updateRequestState(RequestState.PERMIT, id1);
        // 두번쨰 요청은 대여신청을 취소했다고 가정
        requestRepository.cancelRequest(id2, borrower2.getId());

        BorrowedItemDto borrowedItemDto = itemRepository.findById(savedItem.getId());
        // then
        assertThat(borrowedItemDto.getItemId()).isEqualTo(savedItem.getId());
        assertThat(borrowedItemDto.getBorrowerId()).isEqualTo(requester);
    }

    @Test
    @DisplayName("아이디로 찾기(실패)")
    public void findByIdFailedTest() {
        // given
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> itemRepository.findById(192));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND_ITEM.name());
        assertThat(ex.getMessage()).isEqualTo("존재하지 않는 물품입니다.");
    }

    @Test
    @DisplayName("아이디로 삭제")
    public void deleteSuccessTest() {
        // given
        ItemDeleteRequestDto itemDeleteRequestDto = new ItemDeleteRequestDto();
        itemDeleteRequestDto.setDeleteReason("그냥");
        // when
        itemRepository.deleteItem(savedItem.getId(), itemDeleteRequestDto);
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> itemRepository.findById(savedItem.getId()));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND_ITEM.name());
        assertThat(ex.getMessage()).isEqualTo("존재하지 않는 물품입니다.");
    }

    @Test
    @DisplayName("아이템업데이트 테스트")
    public void itemUpdateSuccessTest() {
        // given
        // when
        ItemReviseRequestDto dto = new ItemReviseRequestDto();
        dto.setName("새로운 이름");
        dto.setLocation(savedItem.getLocation());
        dto.setPassword(savedItem.getPassword());
        dto.setPrice(savedItem.getPrice());
        dto.setDeleteReason(null);
        dto.setState(ItemState.BORROWED);

        itemRepository.updateItem(dto, savedItem.getId());
        BorrowedItemDto revisedItem = itemRepository.findById(savedItem.getId());
        // then
        assertThat(revisedItem.getLocation()).isEqualTo(savedItem.getLocation());
        assertThat(revisedItem.getPassword()).isEqualTo(savedItem.getPassword());
        assertThat(revisedItem.getPrice()).isEqualTo(savedItem.getPrice());
        assertThat(revisedItem.getState()).isEqualTo(ItemState.BORROWED);
        assertThat(revisedItem.getName()).isEqualTo("새로운 이름");
    }
}