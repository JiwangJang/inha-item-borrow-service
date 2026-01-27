package com.inha.borrow.backend.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.forAuthTest.admin.WithMockAdmin;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.notice.ModifyNoticeDto;
import com.inha.borrow.backend.model.dto.notice.PostNoticeDto;
import com.inha.borrow.backend.model.entity.Notice;
import com.inha.borrow.backend.service.NoticeService;
import org.springframework.security.test.context.support.WithAnonymousUser;

@WebMvcTest(controllers = NoticeController.class)
@Import(AuthConfig.class)
public class NoticeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NoticeService noticeService;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    BorrowerAuthenticationProvider mockAuthenticationProvider;

    String testTitle = "title";
    String testContent = "content";
    String testAdmin = "test_admin";

    // 공지등록 테스트
    // 공지 등록 (성공-관리자)
    @Test
    @DisplayName("공지 등록(성공-관리자)")
    @WithMockAdmin
    void createNoticeSuccessByAdmin() throws Exception {
        // given
        PostNoticeDto dto = PostNoticeDto.builder()
                .title(testTitle)
                .content(testContent)
                .build();
        when(noticeService.postNotice("test_admin", dto)).thenReturn(1);
        ApiResponse<Map<String, Integer>> expected = new ApiResponse<Map<String, Integer>>(true, Map.of("id", 1));

        // when-then
        mockMvc.perform(post("/notice")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    // 공지 등록 (실패-대여자)
    @Test
    @DisplayName("공지 등록(실패-대여자)")
    @WithMockBorrower
    void createNoticeFailByBorrower() throws Exception {
        // given
        PostNoticeDto dto = PostNoticeDto.builder()
                .title(testTitle)
                .content(testContent)
                .build();
        // when-then
        mockMvc.perform(post("/notice")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    // 공지 등록 (실패-로그인 안 한 사용자)
    @Test
    @DisplayName("공지 등록(실패-로그인 안 한 사용자)")
    @WithAnonymousUser
    void createNoticeFailByAnonymous() throws Exception {
        // given
        PostNoticeDto dto = PostNoticeDto.builder()
                .title(testTitle)
                .content(testContent)
                .build();
        // when-then
        mockMvc.perform(post("/notice")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    // 공지목록 조회 테스트
    // 공지 목록 조회 (성공-로그인 사용자: 대여자)
    @Test
    @DisplayName("공지 목록 조회(성공)")
    @WithMockBorrower
    void getNoticeListSuccessByBorrower() throws Exception {
        // given
        List<Notice> result = new ArrayList<>();
        result.add(new Notice(1, testTitle, testContent, testContent, null, null));
        result.add(new Notice(2, testTitle, testContent, testContent, null, null));
        ApiResponse<List<Notice>> expected = new ApiResponse<List<Notice>>(true, result);
        when(noticeService.findAllNotices()).thenReturn(result);

        // when-then
        mockMvc.perform(get("/notice"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    // 공지 단건 조회 (성공-로그인 사용자: 대여자)
    @Test
    @DisplayName("공지 단건 조회(성공)")
    @WithMockBorrower
    void getNoticeDetailSuccessByBorrower() throws Exception {
        // given
        Notice result = new Notice(1, testTitle, testContent, testContent, null, null);
        ApiResponse<Notice> expected = new ApiResponse<>(true, result);
        when(noticeService.findNoticeById(1)).thenReturn(result);
        // when-then
        mockMvc.perform(get("/notice/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    // 공지 수정 테스트
    // 공지 수정 (성공-관리자)
    @Test
    @DisplayName("공지 수정(성공-관리자)")
    @WithMockAdmin
    void updateNoticeSuccessByAdmin() throws Exception {
        // given
        ModifyNoticeDto dto = new ModifyNoticeDto(1, testTitle, testContent);
        doNothing().when(noticeService).modifyNotice(testAdmin, dto);

        // when-then
        mockMvc.perform(patch("/modify/1")
                .content(objectMapper.writeValueAsBytes(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk());
    }

    // 공지 수정 (실패-대여자)
    @Test
    @DisplayName("공지 수정(실패-대여자)")
    @WithMockBorrower
    void updateNoticeFailByBorrower() throws Exception {
        // given
        ModifyNoticeDto dto = new ModifyNoticeDto(1, testTitle, testContent);

        // when-then
        mockMvc.perform(patch("/modify/1")
                .content(objectMapper.writeValueAsBytes(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    // 공지 수정 (실패-로그인 안 한 사용자)
    @Test
    @DisplayName("공지 수정(실패-로그인 안 한 사용자)")
    @WithAnonymousUser
    void updateNoticeFailByAnonymous() throws Exception {
        // given
        ModifyNoticeDto dto = new ModifyNoticeDto(1, testTitle, testContent);

        // when-then
        mockMvc.perform(patch("/modify/1")
                .content(objectMapper.writeValueAsBytes(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    // 공지 삭제 (성공-관리자)
    @Test
    @DisplayName("공지 삭제(성공-관리자)")
    @WithMockAdmin
    void deleteNoticeSuccessByAdmin() throws Exception {
        // given
        doNothing().when(noticeService).deleteNotice(1);

        // when-then
        mockMvc.perform(delete("/modify/1"))
                .andExpect(status().isNoContent());
    }

    // 공지 삭제 (실패-대여자)
    @Test
    @DisplayName("공지 삭제(실패-대여자)")
    @WithMockBorrower
    void deleteNoticeFailByBorrower() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/modify/1"))
                .andExpect(status().isForbidden());
    }

    // 공지 삭제 (실패-로그인 안 한 사용자)
    @Test
    @DisplayName("공지 삭제(실패-로그인 안 한 사용자)")
    @WithAnonymousUser
    void deleteNoticeFailByAnonymous() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/modify/1"))
                .andExpect(status().isUnauthorized());
    }
}
