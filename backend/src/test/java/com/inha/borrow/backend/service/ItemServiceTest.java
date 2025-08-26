package com.inha.borrow.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class ItemServiceTest {
    @Autowired
    private ItemService itemService;

    @Test
    @DisplayName("대여물품 저장테스트(성공)")
    void createItemSuccessTest() {
        // given
        ItemDto itemDto = new ItemDto("name", "where", "password", 1000);
        // when
        Item savedItem = itemService.createItem(itemDto);
        Item gotItem = itemService.getItemById(savedItem.getId());
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
        List<Item> result = itemService.getAllItem();
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
        Item result = itemService.getItemById(savedItem.getId());
        // then
        assertThat(result.getId()).isEqualTo(savedItem.getId());
    }

    @Test
    @DisplayName("대여물품 단건 조회 테스트(실패-존재하지 않는 대여물품)")
    void getItemByIdFailForNotExistTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(),
                ApiErrorCode.NOT_FOUND.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById(0);
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getErrorMessage(), expected.getErrorMessage());
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
        Item deletedItem = itemService.getItemById(item.getId());
        // then
        assertEquals(deletedItem.getState(), ItemState.DELETED);
    }

    @Test
    @DisplayName("대여물품 삭제 메서드 테스트(실패-존재하지 않는 대여물품)")
    void deleteItemFailForNotExistItemTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(),
                ApiErrorCode.NOT_FOUND.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.deleteItem(0, new ItemDeleteRequestDto("null"));
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getErrorMessage(), expected.getErrorMessage());
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
        Item updatedItem = itemService.getItemById(item.getId());
        // then
        assertEquals(updatedItem.getLocation(), reviseRequestDto.getLocation());
    }

    @Test
    @DisplayName("대여물품 수정 메서드 테스트(실패-존재하지 않는 대여물품)")
    void updateItemDetailFailForNotExistItemTest() {
        // given
        ResourceNotFoundException expected = new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(),
                ApiErrorCode.NOT_FOUND.getMessage());
        // when
        // then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            itemService.updateItemDetail(0, new ItemReviseRequestDto("name", "where2", "password", 1000,
                    ItemState.AFFORD, "null"));
        });

        assertEquals(ex.getErrorCode(), expected.getErrorCode());
        assertEquals(ex.getErrorMessage(), expected.getErrorMessage());
    }
}
