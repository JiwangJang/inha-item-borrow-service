package com.inha.borrow.backend.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.service.ItemService;

@WebMvcTest(controllers = ItemController.class)
@Import(AuthConfig.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    BorrowerAuthenticationProvider mockAuthenticationProvider;

    @Test
    @DisplayName("/items GET 테스트(모든유저 가능)")
    @WithAnonymousUser
    void getAllItem() throws Exception {
        // given
        List<Item> expect = List.of(
                new Item(0, "아이템1", "null", "null", "null", 1000, ItemState.AFFORD));
        when(itemService.getAllItem()).thenReturn(expect);
        // when
        // then
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/items/{item-id} GET 테스트(모든유저 가능)")
    @WithAnonymousUser
    void getItemTest() throws Exception {
        // given
        Item expect = new Item(0, null, null, null, null, 0, null);
        when(itemService.getItemById(0)).thenReturn(expect);
        // when
        // then
        mockMvc.perform(get("/items/0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/items POST 테스트(성공-국원 이상만 접근가능)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void createItemSuccessTest() throws Exception {
        // given
        Item expect = new Item(0, "새로운거", "인하대 어딘가", "비밀임", "delete", 100, ItemState.AFFORD);
        when(itemService.createItem(null)).thenReturn(expect);
        ItemDto itemDto = new ItemDto("새로운거", "인하대 어딘가", "비밀임", 1000);
        // when
        // then
        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("/items POST 테스트(실패-국원 이상만 접근가능)")
    @WithMockUser(authorities = "BORROWER")
    void createItemFailForAuthorityTest() throws Exception {
        // given
        Item expect = new Item(0, "새로운거", "인하대 어딘가", "비밀임", "delete", 100, ItemState.AFFORD);
        when(itemService.createItem(null)).thenReturn(expect);
        ItemDto itemDto = new ItemDto("새로운거", "인하대 어딘가", "비밀임", 1000);
        // when
        // then
        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/items POST 테스트(실패-로그인 안함")
    @WithAnonymousUser
    void createItemFailForNotAuthenticationTest() throws Exception {
        // given
        Item expect = new Item(0, "새로운거", "인하대 어딘가", "비밀임", "delete", 100, ItemState.AFFORD);
        when(itemService.createItem(null)).thenReturn(expect);
        ItemDto itemDto = new ItemDto("새로운거", "인하대 어딘가", "비밀임", 1000);
        // when
        // then
        mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/items/{id} PUT 테스트(성공-국원 이상만 접근가능)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void updateItemSuccessTest() throws Exception {
        // given
        ItemReviseRequestDto dto = new ItemReviseRequestDto("새로운거", "null", "null", 1000, ItemState.AFFORD, "null");
        doNothing().when(itemService).updateItemDetail(0, dto);
        // when
        // then
        mockMvc.perform(put("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("/items/{id} PUT 테스트(실패-국원 이상만 접근가능)")
    @WithMockUser(authorities = "BORROWER")
    void updateItemFailForAuthorityTest() throws Exception {
        // given
        ItemReviseRequestDto dto = new ItemReviseRequestDto("새로운거", "null", "null", 1000, ItemState.AFFORD, "null");
        doNothing().when(itemService).updateItemDetail(0, dto);
        // when
        // then
        mockMvc.perform(put("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/items/{id} PUT 테스트(실패-로그인 안함")
    @WithAnonymousUser
    void updateItemFailForNotAuthenticationTest() throws Exception {
        // given
        ItemReviseRequestDto dto = new ItemReviseRequestDto("새로운거", "null", "null", 1000, ItemState.AFFORD, "null");
        doNothing().when(itemService).updateItemDetail(0, dto);
        // when
        // then
        mockMvc.perform(put("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/items/{id} DELETE 테스트(성공-국원 이상만 접근가능)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void deleteItemSuccessTest() throws Exception {
        // given
        ItemDeleteRequestDto dto = new ItemDeleteRequestDto("삭제이유");
        doNothing().when(itemService).deleteItem(0, dto);
        // when
        // then
        mockMvc.perform(delete("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("/items/{id} DELETE 테스트(실패-국원 이상만 접근가능)")
    @WithMockUser(authorities = "BORROWER")
    void deleteItemFailForAuthorityTest() throws Exception {
        // given
        ItemDeleteRequestDto dto = new ItemDeleteRequestDto("삭제이유");
        doNothing().when(itemService).deleteItem(0, dto);
        // when
        // then
        mockMvc.perform(delete("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/items/{id} DELETE 테스트(실패-로그인 안함")
    @WithAnonymousUser
    void deleteItemFailForNotAuthenticationTest() throws Exception {
        // given
        ItemDeleteRequestDto dto = new ItemDeleteRequestDto("삭제이유");
        doNothing().when(itemService).deleteItem(0, dto);
        // when
        // then
        mockMvc.perform(delete("/items/1")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }
}