package com.inha.borrow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.model.dto.user.PatchPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchEmailDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowerService borrowerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("모든 대여자 조회")
    @WithMockUser
    void findAllBorrower() throws Exception {
        Borrower borrower = Borrower.builder().id("123").name("홍길동").build();
        given(borrowerService.findAll()).willReturn(List.of(borrower));

        mockMvc.perform(get("/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("123"))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"));
    }

    @Test
    @DisplayName("대여자 id로 조회")
    @WithMockUser(username = "123")
    void findById() throws Exception {
        Borrower borrower = Borrower.builder().id("123").name("홍길동").build();
        given(borrowerService.findById("123")).willReturn(borrower);

        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("123"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    @DisplayName("비밀번호 수정")
    @WithMockUser(username = "123")
    void patchPassword() throws Exception {
        PatchPasswordDto dto = new PatchPasswordDto("oldPass", "NewPass123!");
        doNothing().when(borrowerService).patchPassword(dto, "123");

        mockMvc.perform(patch("/info/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 수정")
    @WithMockUser(username = "123")
    void patchEmail() throws Exception {
        doNothing().when(borrowerService).patchEmail("123", "test@example.com");

        PatchEmailDto dto = new PatchEmailDto("test@example.com");

        mockMvc.perform(patch("/info/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이름 수정")
    @WithMockUser(username = "123")
    void patchName() throws Exception {
        doNothing().when(borrowerService).patchName("123", "새이름");

        mockMvc.perform(patch("/info/name")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"새이름\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전화번호 수정")
    @WithMockUser(username = "123")
    void patchPhoneNumber() throws Exception {
        doNothing().when(borrowerService).patchPhoneNumber("01012345678", "123");

        mockMvc.perform(patch("/info/phonenum")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"01012345678\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ban 상태 수정")
    @WithMockUser
    void patchBan() throws Exception {
        doNothing().when(borrowerService).patchBan(true, "123");

        mockMvc.perform(patch("/123/info/ban")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isOk());
    }
}
