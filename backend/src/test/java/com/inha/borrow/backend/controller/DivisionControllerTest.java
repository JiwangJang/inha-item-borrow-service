package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.response.ErrorResponse;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.service.DivisionService;

@WebMvcTest(controllers = DivisionController.class)
@Import(AuthConfig.class)
public class DivisionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DivisionService divisionService;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    BorrowerAuthenticationProvider mockAuthenticationProvider;

    @Test
    @DisplayName("부서목록 조회 테스트(성공-국장 이상만 접근 가능)")
    @WithMockUser(authorities = "DIVISION_HEAD")
    void findAllDivisionsSuccessTest() throws Exception {
        // given
        List<Division> expectedResult = List.of(new Division("TEST", "테스트"));
        ApiResponse<List<Division>> expectedResponse = new ApiResponse<>(true, expectedResult);
        when(divisionService.findAllDivisions()).thenReturn(expectedResult);
        // when
        // then
        mockMvc.perform(get("/divisions"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("부서목록 조회 테스트(실패-국장 이상만 접근 가능)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void findAllDivisionsFailForAuthorityTest() throws Exception {
        // given
        List<Division> expected = List.of(new Division("TEST", "테스트"));
        when(divisionService.findAllDivisions()).thenReturn(expected);
        // when
        // then
        mockMvc.perform(get("/divisions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서목록 조회 테스트(실패-로그인 안 한 사용자)")
    @WithAnonymousUser
    void findAllDivisionsFailForNoLoginTest() throws Exception {
        // given
        List<Division> expected = List.of(new Division("TEST", "테스트"));
        when(divisionService.findAllDivisions()).thenReturn(expected);
        // when
        // then
        mockMvc.perform(get("/divisions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("부서 저장 테스트(성공-학생회장만 접근 가능)")
    @WithMockUser(authorities = "PRESIDENT")
    void saveDivisionSuccessTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트");
        ApiResponse<Division> expectedResponse = new ApiResponse<Division>(true, null);
        doNothing().when(divisionService).saveDivision(divisionDto);
        // when
        // then
        mockMvc.perform(post("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("부서 저장 테스트(실패-학생회장만 접근 가능)")
    @WithMockUser(authorities = "VICE_PRESIDENT")
    void saveDivisionFailForAuthorityTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트");
        doNothing().when(divisionService).saveDivision(divisionDto);
        // when
        // then
        mockMvc.perform(post("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서 저장(실패-로그인 안한 사용자)")
    @WithAnonymousUser
    void saveDivisionFailForNotLoginTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트");
        doNothing().when(divisionService).saveDivision(divisionDto);
        // when
        // then
        mockMvc.perform(post("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    // 똑같은 DTO 사용해서 나머지 메서드(PATCH, DELETE)도 이거로 대체
    @ParameterizedTest
    @DisplayName("부서 저장 테스트(실패-허용되지 않는 값)")
    @CsvSource({
            "' ', 테스트 부서, 부서코드는 필수입니다.",
            "TEST, ' ', 부서명은 필수입니다."
    })
    @WithMockUser(authorities = "PRESIDENT")
    void saveDivisionFailForInvalidValueTest(String code, String name, String errorMsg) throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto(code, name);
        doNothing().when(divisionService).saveDivision(any());
        ErrorResponse expectedErrorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMsg);
        ApiResponse<ErrorResponse> exepectedApiResponse = new ApiResponse<ErrorResponse>(false, expectedErrorResponse);
        // when
        // then
        mockMvc.perform(post("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(exepectedApiResponse)));
    }

    @Test
    @DisplayName("부서명 수정 테스트(성공)")
    @WithMockUser(authorities = "PRESIDENT")
    void updateDivisionSuccessTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).updateDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("부서명 수정 테스트(실패-권한 문제)")
    @WithMockUser(authorities = "VICE_PRESIDENT")
    void updateDivisionFailForAuthorityTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).updateDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서명수정 테스트(실패-로그인하지 않은 사용자)")
    @WithAnonymousUser
    void updateDivisionFailForNoLogin() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).updateDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("부서명 삭제 테스트(성공)")
    @WithMockUser(authorities = "PRESIDENT")
    void deleteDivisionSuccessTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).deleteDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("부서명 삭제 테스트(실패-권한 문제)")
    @WithMockUser(authorities = "VICE_PRESIDENT")
    void deleteDivisionFailForAuthorityTest() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).deleteDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서명 삭제 테스트(실패-로그인하지 않은 사용자)")
    @WithAnonymousUser
    void deleteDivisionFailForNoLogin() throws Exception {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "테스트 부서");
        doNothing().when(divisionService).deleteDivision(any());
        // when
        // then
        mockMvc.perform(patch("/divisions")
                .content(objectMapper.writeValueAsString(divisionDto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }
}
