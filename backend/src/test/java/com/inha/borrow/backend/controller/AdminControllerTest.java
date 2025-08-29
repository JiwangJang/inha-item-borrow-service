package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.forAuthTest.admin.WithMockAdmin;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.service.AdminService;

@WebMvcTest(AdminController.class)
@Import(AuthConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    private BorrowerAuthenticationProvider mockBorrowerAuthenticationProvider;

    @Test
    @DisplayName("GET /admins 성공")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void findAllAdminsSuccess() throws Exception {
        Admin a1 = Admin.builder().id("admin1").build();
        Admin a2 = Admin.builder().id("admin2").build();
        when(adminService.findAllAdmins()).thenReturn(List.of(a1, a2));
        mockMvc.perform(get("/admins")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admins/info 성공")
    @WithMockUser(username = "user1234", authorities = "DIVISION_MEMBER")
    void findByIdSuccess() throws Exception {
        String id = "user1234";
        Admin admin = Admin.builder().id(id).build();
        when(adminService.findById(id)).thenReturn(admin);
        mockMvc.perform(get("/admins/info")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admins/info 실패 - 미존재")
    @WithMockUser(username = "no_such", authorities = "DIVISION_MEMBER")
    void findByIdFailNotFound() throws Exception {
        String id = "no_such";
        when(adminService.findById(id)).thenThrow(new ResourceNotFoundException("NOT_FOUND", "없음"));
        mockMvc.perform(get("/admins/info")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /admins/sub-admin 성공")
    @WithMockUser(authorities = "PRESIDENT")
    void saveAdminSuccess() throws Exception {
        SaveAdminDto dto = SaveAdminDto.builder()
                .id("user1")
                .name("유저")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-1234-5678")
                .email("u@test.com")
                .division("TEST")
                .build();
        doNothing().when(adminService).saveAdmin(any(SaveAdminDto.class));
        mockMvc.perform(post("/admins/sub-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /admins/sub-admin 실패 - 유효성 위반")
    @WithMockUser(authorities = "PRESIDENT")
    void saveAdminFailValidation() throws Exception {
        String body = "{\n" +
                "  \"id\": \"abc\",\n" +
                "  \"name\": \"유저\",\n" +
                "  \"position\": \"DIVISION_MEMBER\",\n" +
                "  \"phonenumber\": \"010-1234-5678\",\n" +
                "  \"email\": \"u@test.com\",\n" +
                "  \"division\": \"TEST\"\n" +
                "}";
        mockMvc.perform(post("/admins/sub-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /admins/info 성공")
    @WithMockUser(username = "user999", authorities = "DIVISION_MEMBER")
    void updateAdminInfoSuccess() throws Exception {
        String id = "user999";
        UpdateAdminInfoDto dto = UpdateAdminInfoDto.builder()
                .name("새이름")
                .phonenumber("010-0000-0000")
                .email("new@test.com")
                .build();
        doNothing().when(adminService).updateAdminInfo(eq(id), any(UpdateAdminInfoDto.class));
        mockMvc.perform(patch("/admins/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /admins/info 실패 - 미존재")
    @WithMockUser(username = "user404", authorities = "DIVISION_MEMBER")
    void updateAdminInfoFailNotFound() throws Exception {
        String id = "user404";
        UpdateAdminInfoDto dto = UpdateAdminInfoDto.builder()
                .name("새이름")
                .phonenumber("010-0000-0000")
                .email("new@test.com")
                .build();
        doThrow(new ResourceNotFoundException("NOT_FOUND", "없음")).when(adminService)
                .updateAdminInfo(eq(id), any(UpdateAdminInfoDto.class));
        mockMvc.perform(patch("/admins/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /admins/info/password 성공")
    @WithMockUser(username = "userpw1", authorities = "DIVISION_MEMBER")
    void updatePasswordSuccess() throws Exception {
        String id = "userpw1";
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOriginPassword("old");
        dto.setNewPassword("new");
        doNothing().when(adminService).updatePassword(eq(id), any(UpdatePasswordDto.class));
        mockMvc.perform(patch("/admins/info/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /admins/info/password 실패 - 기존 비번 불일치")
    @WithMockUser(username = "userpw2", authorities = "DIVISION_MEMBER")
    void updatePasswordFailInvalid() throws Exception {
        String id = "userpw2";
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOriginPassword("wrong");
        dto.setNewPassword("new");
        doThrow(new InvalidValueException("INCORRECT_PASSWORD", "비밀번호 오류")).when(adminService)
                .updatePassword(eq(id), any(UpdatePasswordDto.class));
        mockMvc.perform(patch("/admins/info/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /admins/info/{id}/position 성공")
    @WithMockAdmin(role = Role.PRESIDENT, division = "TEST")
    void updatePositionSuccess() throws Exception {
        String targetId = "target1";
        UpdatePositionDto dto = new UpdatePositionDto(Role.DIVISION_HEAD);
        doNothing().when(adminService).updatePosition(any(Admin.class), eq(targetId), any(UpdatePositionDto.class));
        mockMvc.perform(patch("/admins/info/{id}/position", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /admins/info/{id}/position 실패 - 권한 불가")
    @WithMockAdmin(role = Role.DIVISION_HEAD, division = "TEST")
    void updatePositionFailForbidden() throws Exception {
        String targetId = "target2";
        UpdatePositionDto dto = new UpdatePositionDto(Role.DIVISION_HEAD);
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(adminService).updatePosition(any(Admin.class), eq(targetId), any(UpdatePositionDto.class));
        mockMvc.perform(patch("/admins/info/{id}/position", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /admins/info/{id}/division 성공")
    @WithMockAdmin(role = Role.PRESIDENT, division = "TEST")
    void updateDivisionSuccess() throws Exception {
        String targetId = "target3";
        UpdateDivisionDto dto = new UpdateDivisionDto("OTHER");
        doNothing().when(adminService).updateDivision(any(Admin.class), eq(targetId), any(UpdateDivisionDto.class));
        mockMvc.perform(patch("/admins/info/{id}/division", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /admins/info/{id}/division 실패 - 권한 불가")
    @WithMockAdmin(role = Role.DIVISION_HEAD, division = "TEST")
    void updateDivisionFailForbidden() throws Exception {
        String targetId = "target4";
        UpdateDivisionDto dto = new UpdateDivisionDto("OTHER");
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(adminService).updateDivision(any(Admin.class), eq(targetId), any(UpdateDivisionDto.class));
        mockMvc.perform(patch("/admins/info/{id}/division", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /admins/sub-admin/{id} 성공")
    @WithMockUser(authorities = "PRESIDENT")
    void deleteAdminSuccess() throws Exception {
        doNothing().when(adminService).deleteAdmin("del1");
        mockMvc.perform(delete("/admins/sub-admin/{id}", "del1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /admins/sub-admin/{id} 실패 - 미존재")
    @WithMockUser(authorities = "PRESIDENT")
    void deleteAdminFailNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("NOT_FOUND", "없음")).when(adminService).deleteAdmin("del404");
        mockMvc.perform(delete("/admins/sub-admin/{id}", "del404")).andExpect(status().isNotFound());
    }
}

