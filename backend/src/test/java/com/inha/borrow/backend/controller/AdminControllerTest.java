package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.handler.GlobalErrorHandler;
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

@WebMvcTest(controllers = AdminController.class)
@Import({ GlobalErrorHandler.class, AuthConfig.class })
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

        // --------------------
        // Unauthenticated access tests
        // --------------------

        @Test
        @DisplayName("인증 없음 - GET /admins 401")
        void unauthenticated_getAdmins() throws Exception {
                mockMvc.perform(get("/admins"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - GET /admins/info 401")
        void unauthenticated_getAdminInfo() throws Exception {
                mockMvc.perform(get("/admins/info"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - POST /admins/sub-admin 401")
        void unauthenticated_postSubAdmin() throws Exception {
                String body = "{\n" +
                                "  \"id\": \"user1\",\n" +
                                "  \"name\": \"유저\",\n" +
                                "  \"position\": \"DIVISION_MEMBER\",\n" +
                                "  \"phonenumber\": \"010-1234-5678\",\n" +
                                "  \"email\": \"u@test.com\",\n" +
                                "  \"division\": \"TEST\"\n" +
                                "}";
                mockMvc.perform(post("/admins/sub-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - PATCH /admins/info 401")
        void unauthenticated_patchAdminInfo() throws Exception {
                UpdateAdminInfoDto dto = UpdateAdminInfoDto.builder()
                                .name("a").phonenumber("010").email("a@test.com").build();
                mockMvc.perform(patch("/admins/info")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - PATCH /admins/info/password 401")
        void unauthenticated_patchPassword() throws Exception {
                UpdatePasswordDto dto = new UpdatePasswordDto("Abcdefg1!", "origin");
                mockMvc.perform(patch("/admins/info/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - PATCH /admins/info/{id}/position 401")
        void unauthenticated_patchPosition() throws Exception {
                UpdatePositionDto dto = new UpdatePositionDto(Role.DIVISION_HEAD);
                mockMvc.perform(patch("/admins/info/{id}/position", "t1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - PATCH /admins/info/{id}/division 401")
        void unauthenticated_patchDivision() throws Exception {
                UpdateDivisionDto dto = new UpdateDivisionDto("TEST");
                mockMvc.perform(patch("/admins/info/{id}/division", "t1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없음 - DELETE /admins/sub-admin/{id} 401")
        void unauthenticated_deleteSubAdmin() throws Exception {
                mockMvc.perform(delete("/admins/sub-admin/{id}", "d1"))
                                .andExpect(status().isUnauthorized());
        }

        // --------------------
        // Borrower role access tests (should be 403)
        // --------------------

        @Test
        @DisplayName("BORROWER - GET /admins 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_getAdmins_forbidden() throws Exception {
                mockMvc.perform(get("/admins")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - GET /admins/info 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_getAdminInfo_forbidden() throws Exception {
                mockMvc.perform(get("/admins/info")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - PATCH /admins/info 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_patchAdminInfo_forbidden() throws Exception {
                UpdateAdminInfoDto dto = UpdateAdminInfoDto.builder()
                                .name("a").phonenumber("010").email("a@test.com").build();
                mockMvc.perform(patch("/admins/info")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - PATCH /admins/info/password 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_patchPassword_forbidden() throws Exception {
                UpdatePasswordDto dto = new UpdatePasswordDto("Abcdefg1!", "origin");
                mockMvc.perform(patch("/admins/info/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - PATCH /admins/info/{id}/position 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_patchPosition_forbidden() throws Exception {
                UpdatePositionDto dto = new UpdatePositionDto(Role.DIVISION_HEAD);
                mockMvc.perform(patch("/admins/info/{id}/position", "t1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - PATCH /admins/info/{id}/division 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_patchDivision_forbidden() throws Exception {
                UpdateDivisionDto dto = new UpdateDivisionDto("TEST");
                mockMvc.perform(patch("/admins/info/{id}/division", "t1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BORROWER - POST /admins/sub-admin 403")
        @WithMockUser(authorities = "BORROWER")
        void borrower_postSubAdmin_forbidden() throws Exception {
                SaveAdminDto dto = SaveAdminDto.builder()
                                .id("user1").name("유저").position(Role.DIVISION_MEMBER)
                                .phonenumber("010-1234-5678").email("u@test.com").division("TEST").build();
                mockMvc.perform(post("/admins/sub-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

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
        @WithMockAdmin
        void findByIdSuccess() throws Exception {
                String id = "user1234";
                Admin admin = Admin.builder().id(id).build();
                when(adminService.findById(id)).thenReturn(admin);
                mockMvc.perform(get("/admins/info")).andExpect(status().isOk());
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
        @DisplayName("PATCH /admins/info 성공")
        @WithMockAdmin
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
        @DisplayName("PATCH /admins/info/password 성공")
        @WithMockAdmin
        void updatePasswordSuccess() throws Exception {
                String id = "userpw1";
                UpdatePasswordDto dto = new UpdatePasswordDto();
                dto.setOriginPassword("oldaaaaA1!");
                dto.setNewPassword("newaaaaA1!");
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
                doNothing().when(adminService).updatePosition(any(Admin.class), eq(targetId),
                                any(UpdatePositionDto.class));
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
                doThrow(new AccessDeniedException("forbidden"))
                                .when(adminService)
                                .updatePosition(any(Admin.class), eq(targetId), any(UpdatePositionDto.class));
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
                doNothing().when(adminService).updateDivision(any(Admin.class), eq(targetId),
                                any(UpdateDivisionDto.class));
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
                doThrow(new AccessDeniedException("forbidden"))
                                .when(adminService)
                                .updateDivision(any(Admin.class), eq(targetId), any(UpdateDivisionDto.class));
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

        // --------------------
        // DELETE with non-PRESIDENT roles should be forbidden
        // --------------------

        @ParameterizedTest
        @CsvSource({ "DIVISION_MEMBER", "DIVISION_HEAD", "VICE_PRESIDENT", "BORROWER" })
        @DisplayName("DELETE /admins/sub-admin/{id} 403 - PRESIDENT 이외 권한")
        void deleteAdmin_forbidden_other_roles(String role) throws Exception {
                mockMvc.perform(delete("/admins/sub-admin/{id}", "del2")
                                .with(user("u").authorities(new SimpleGrantedAuthority(role))))
                                .andExpect(status().isForbidden());
        }

        // --------------------
        // Parameterized validation tests
        // --------------------

        @ParameterizedTest
        @CsvSource(value = {
                        // 아이디, 이름, 직책, 핸드폰번호, 이메일, 부서
                        "abc, 유저, DIVISION_MEMBER, 010-1, u@test.com, TEST",
                        "user1, '', DIVISION_MEMBER, 010-1, u@test.com, TEST",
                        "user1, 유저, null, 010-1, u@test.com, TEST",
                        "user1, 유저, DIVISION_MEMBER, '', u@test.com, TEST",
                        "user1, 유저, DIVISION_MEMBER, 010-1, '', TEST",
                        "user1, 유저, DIVISION_MEMBER, 010-1, u@test.com, null"
        }, quoteCharacter = '\'')
        @DisplayName("POST /admins/sub-admin 실패 - 유효성 위반(파라미터화)")
        @WithMockUser(authorities = "PRESIDENT")
        void saveAdminFailValidationParameterized(String id, String name, String positionStr, String phonenumber,
                        String email, String divisionStr) throws Exception {
                Role position = (positionStr == null || positionStr.equals("null") || positionStr.isBlank())
                                ? null
                                : Role.valueOf(positionStr);
                String division = (divisionStr == null || divisionStr.equals("null")) ? null : divisionStr;

                SaveAdminDto dto = SaveAdminDto.builder()
                                .id(id)
                                .name(name)
                                .position(position)
                                .phonenumber(phonenumber)
                                .email(email)
                                .division(division)
                                .build();

                mockMvc.perform(post("/admins/sub-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @CsvSource(value = {
                        // 이름, 전화번호, 이메일
                        "이름1, ' ', ads@naver.com",
                        "이름2, 010-0000-0000, ' '",
                        "' ', 010-0000-0000, EXAM@naver.com"
        })
        @DisplayName("PATCH /admins/info 실패 - 유효성 위반(파라미터화)")
        @WithMockUser(username = "user999", authorities = "DIVISION_MEMBER")
        void updateAdminInfoFailValidationParameterized(String name, String phonenumber, String email)
                        throws Exception {
                UpdateAdminInfoDto dto = new UpdateAdminInfoDto(name, phonenumber, email);
                mockMvc.perform(patch("/admins/info")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @CsvSource({
                        "short1!", // too short
                        "lowercase1!", // no uppercase
                        "UPPERCASE1!", // no lowercase
                        "Abcdefghij", // no special
                        "Abcdefghi!" // no digit
        })
        @DisplayName("PATCH /admins/info/password 실패 - 유효성 위반(파라미터화)")
        @WithMockUser(username = "userpw3", authorities = "DIVISION_MEMBER")
        void updatePasswordFailValidationParameterized(String invalidNewPassword) throws Exception {
                UpdatePasswordDto dto = new UpdatePasswordDto(invalidNewPassword, "origin");
                mockMvc.perform(patch("/admins/info/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PATCH /admins/info/{id}/position 실패 - 유효성 위반(null)")
        @WithMockAdmin(role = Role.PRESIDENT, division = "TEST")
        void updatePositionFailValidation() throws Exception {
                String targetId = "targetX";
                String body = "{\"position\":null}";
                mockMvc.perform(patch("/admins/info/{id}/position", targetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PATCH /admins/info/{id}/division 실패 - 유효성 위반(blank)")
        @WithMockAdmin(role = Role.PRESIDENT, division = "TEST")
        void updateDivisionFailValidation() throws Exception {
                String targetId = "targetY";
                String body = "{\"division\":\"\"}";
                mockMvc.perform(patch("/admins/info/{id}/division", targetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }
}
