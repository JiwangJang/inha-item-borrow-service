package com.inha.borrow.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Collections;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.repository.RequestRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;

@SpringBootTest
@Transactional
public class ItemServiceTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private BorrowerRepository borrowerRepository;

    private User adminUser() {
        return Admin.builder()
                .id("admin")
                .password("pw")
                .email("a@a.com")
                .name("admin")
                .phonenumber("000")
                .refreshToken(null)
                .divisionCode("DIV")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(Role.DIVISION_MEMBER.name())))
                .build();
    }

    private User borrowerUser() {
        return borrowerUser("borrower");
    }

    private User borrowerUser(String id) {
        return Borrower.builder()
                .id(id)
                .password("pw")
                .email("b@b.com")
                .name("borrower")
                .phonenumber("000")
                .refreshToken(null)
                .ban(false)
                .withDrawal(false)
                .studentNumber("20240001")
                .accountNumber("111-1111")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(Role.BORROWER.name())))
                .build();
    }

    private int insertPermittedBorrowRequest(int itemId, String borrowerId) {
        // Ensure FK constraint: borrower must exist
        borrowerRepository.save(BorrowerDto.builder()
                .id(borrowerId)
                .password("pw")
                .email(borrowerId + "@test.com")
                .name("name")
                .phonenumber("010-0000-0000")
                .studentNumber("20240001")
                .accountNumber("111-1111")
                .refreshToken("123")
                .build());
        SaveRequestDto saveRequest = SaveRequestDto.builder()
                .itemId(itemId)
                .borrowerId(borrowerId)
                .returnAt(Timestamp.from(Instant.now()))
                .borrowerAt(Timestamp.from(Instant.now()))
                .type(RequestType.BORROW)
                .build();
        int reqId = requestRepository.save(saveRequest);
        requestRepository.evaluationRequest(RequestState.PERMIT, reqId);
        return reqId;
    }

    @Test
    @DisplayName("대여물품 저장테스트(성공)")
    void createItemSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        // when
        Item savedItem = itemService.createItem(itemDto);
        Item gotItem = itemService.getItemById(adminUser(), savedItem.getId());
        // then
        assertThat(gotItem.getId()).isEqualTo(savedItem.getId());
        assertThat(gotItem.getName()).isEqualTo(savedItem.getName());
        assertThat(gotItem.getLocation()).isEqualTo(savedItem.getLocation());
        assertThat(gotItem.getPassword()).isEqualTo(savedItem.getPassword());
        assertThat(gotItem.getPrice()).isEqualTo(savedItem.getPrice());
    }

    @Test
    @DisplayName("대여물품 전체조회 테스트(성공)")
    void getAllItemSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        // when
        itemService.createItem(itemDto);
        List<Item> result = itemService.getAllItem(adminUser());
        // then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("대여물품 단건 조회 테스트(성공)")
    void getItemByIdSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        // when
        Item savedItem = itemService.createItem(itemDto);
        Item result = itemService.getItemById(borrowerUser(), savedItem.getId());
        // then
        assertThat(result.getId()).isEqualTo(savedItem.getId());
    }

    @Test
    @DisplayName("대여물품 단건 조회 테스트(실패-존재하지 않는 대여물품)")
    void getItemByIdFailForNotExistTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND_ITEM.name(),
                ApiErrorCode.NOT_FOUND_ITEM.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById(adminUser(), 0);
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getMessage(), "존재하지 않는 물품입니다.");
    }

    @Test
    @DisplayName("대여물품 삭제 메서드 태스트(성공)")
    void deleteItemSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        ItemDeleteRequestDto deleteRequestDto = new ItemDeleteRequestDto("삭제이유");
        // when
        Item item = itemService.createItem(itemDto);
        itemService.deleteItem(item.getId(), deleteRequestDto);
        // then: 현재 리포지토리는 삭제된 아이템을 조회 시 NOT_FOUND를 던집니다.
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById(adminUser(), item.getId());
        });
        assertEquals(ex.getErrorCode(), ApiErrorCode.NOT_FOUND_ITEM.name());
        assertEquals(ex.getMessage(), "존재하지 않는 물품입니다.");
    }

    @Test
    @DisplayName("대여물품 삭제 메서드 테스트(실패-존재하지 않는 대여물품)")
    void deleteItemFailForNotExistItemTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND_ITEM.name(),
                ApiErrorCode.NOT_FOUND_ITEM.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.deleteItem(0, new ItemDeleteRequestDto("null"));
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getMessage(), expected.getMessage());
    }

    @Test
    @DisplayName("대여물품 수정 메서드 테스트(성공)")
    void updateItemDetailSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        ItemReviseRequestDto reviseRequestDto = new ItemReviseRequestDto("name", "where2", "password", 1000,
                ItemState.AFFORD, "null");
        // when
        Item item = itemService.createItem(itemDto);
        itemService.updateItemDetail(item.getId(), reviseRequestDto);
        Item updatedItem = itemService.getItemById(adminUser(), item.getId());
        // then
        assertEquals(updatedItem.getLocation(), reviseRequestDto.getLocation());
    }

    @Test
    @DisplayName("getItemById - 관리자이면 전체 정보 노출")
    void getItemById_AsAdmin_AllInfo() {
        // given
        ItemDto itemDto = new ItemDto("umbrella", "somewhere", "1234", 1000);
        Item saved = itemService.createItem(itemDto);
        insertPermittedBorrowRequest(saved.getId(), "borrowerA");
        // when
        Item result = itemService.getItemById(adminUser(), saved.getId());
        // then
        assertThat(result.getPassword()).isEqualTo(saved.getPassword());
        assertThat(result.getLocation()).isEqualTo(saved.getLocation());
        assertThat(result.getPrice()).isEqualTo(saved.getPrice());
    }

    @Test
    @DisplayName("getItemById - 대여자 본인이면 전체 정보 노출")
    void getItemById_AsBorrowerSelf_AllInfo() {
        // given
        ItemDto itemDto = new ItemDto("umbrella", "somewhere", "1234", 1000);
        Item saved = itemService.createItem(itemDto);
        insertPermittedBorrowRequest(saved.getId(), "me");
        // when
        Item result = itemService.getItemById(borrowerUser("me"), saved.getId());
        // then
        assertThat(result.getPassword()).isEqualTo(saved.getPassword());
        assertThat(result.getLocation()).isEqualTo(saved.getLocation());
    }

    @Test
    @DisplayName("getItemById - 타인은 부분 정보만 노출")
    void getItemById_AsOtherBorrower_PartialInfo() {
        // given
        ItemDto itemDto = new ItemDto("umbrella", "somewhere", "1234", 1000);
        Item saved = itemService.createItem(itemDto);
        insertPermittedBorrowRequest(saved.getId(), "realBorrower");
        // when
        Item result = itemService.getItemById(borrowerUser("other"), saved.getId());
        // then
        assertThat(result.getPassword()).isNull();
        assertThat(result.getLocation()).isNull();
        assertThat(result.getName()).isEqualTo(saved.getName());
        assertThat(result.getPrice()).isEqualTo(saved.getPrice());
    }

    @Test
    @DisplayName("대여물품 수정 메서드 테스트(실패-존재하지 않는 대여물품)")
    void updateItemDetailFailForNotExistItemTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND_ITEM.name(),
                ApiErrorCode.NOT_FOUND_ITEM.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.updateItemDetail(0, new ItemReviseRequestDto("name", "where2", "password", 1000,
                    ItemState.AFFORD, "null"));
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getMessage(), expected.getMessage());
    }
}
