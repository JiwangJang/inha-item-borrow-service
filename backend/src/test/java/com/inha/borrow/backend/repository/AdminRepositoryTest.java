package com.inha.borrow.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.service.JwtTokenService;

@JdbcTest
@Import({ AdminRepository.class, AdminRepositoryTest.TestConfig.class })
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class AdminRepositoryTest {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        JwtTokenService jwtTokenService() {
            return new JwtTokenService() {
                @Override
                public String createToken(String id) {
                    return "test-token-" + id;
                }
            };
        }
    }

    private void ensureDivision(String code, String name) {
        String sql = "INSERT INTO division(code, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name);";
        jdbcTemplate.update(sql, code, name);
    }

    private void ensureAdminRole(Role role, int level) {
        String sql = "INSERT INTO admin_role(role, level) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level);";
        jdbcTemplate.update(sql, role.name(), level);
    }

    private void insertAdmin(String id, Role role, String division) {
        // Clean potential leftovers with same id
        jdbcTemplate.update("DELETE FROM admin WHERE id = ?", id);
        String sql = "INSERT INTO admin(id, password, email, name, phonenumber, position, division, refresh_token, is_delete) VALUES(?, ?, ?, ?, ?, ?, ?, ?, false);";
        jdbcTemplate.update(sql, id, "$2a$10$abcdefghijklmnopqrstuv", id + "@test.com", "테스터", "010-0000-0000",
                role.name(), division, "refresh" + id);
    }

    private Admin buildActor(String id, Role role, String division) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));
        return Admin.builder()
                .id(id)
                .password("")
                .email(id + "@test.com")
                .name("액터")
                .phonenumber("010-0000-0000")
                .authorities(authorities)
                .refreshToken("r-" + id)
                .divisionCode(division)
                .build();
    }

    @Test
    @DisplayName("findById 성공")
    void findByIdSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "repo_find_by_id";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST");

        // when
        Admin found = adminRepository.findById(id);

        // then
        assertEquals(id, found.getId());
        assertEquals("TEST", found.getDivisionCode());
    }

    @Test
    @DisplayName("findById 실패 - 미존재")
    void findByIdNotFoundTest() {
        assertThrows(ResourceNotFoundException.class, () -> adminRepository.findById("no_such_id"));
    }

    @Test
    @DisplayName("findAllAdmins 성공")
    void findAllAdminsSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        insertAdmin("repo_all_1", Role.DIVISION_MEMBER, "TEST");
        insertAdmin("repo_all_2", Role.DIVISION_MEMBER, "TEST");

        // when
        List<Admin> all = adminRepository.findAllAdmins();

        // then
        long count = all.stream().filter(a -> a.getId().equals("repo_all_1") || a.getId().equals("repo_all_2")).count();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("saveAdmin 성공")
    void saveAdminSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "repo_save_admin";
        jdbcTemplate.update("DELETE FROM admin WHERE id = ?", id);
        SaveAdminDto dto = SaveAdminDto.builder()
                .id(id)
                .name("신규관리자")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-1234-5678")
                .email("save@test.com")
                .division("TEST")
                .build();

        // when
        adminRepository.saveAdmin(dto);

        // then
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin WHERE id = ?", Integer.class, id);
        assertEquals(1, cnt);
        String rt = jdbcTemplate.queryForObject("SELECT refresh_token FROM admin WHERE id = ?", String.class, id);
        assertEquals("test-token-" + id, rt);
    }

    @Test
    @DisplayName("updateAdminInfo 성공")
    void updateAdminInfoSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "repo_update_info";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST");
        UpdateAdminInfoDto info = UpdateAdminInfoDto.builder()
                .name("변경된이름")
                .phonenumber("010-9999-9999")
                .email("changed@test.com")
                .build();

        // when
        adminRepository.updateAdminInfo(id, info);

        // then
        var row = jdbcTemplate.queryForMap("SELECT name, phonenumber, email FROM admin WHERE id = ?", id);
        assertEquals("변경된이름", row.get("name"));
        assertEquals("010-9999-9999", row.get("phonenumber"));
        assertEquals("changed@test.com", row.get("email"));
    }

    @Test
    @DisplayName("updatePassword 성공")
    void updatePasswordSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "repo_update_pwd";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST");
        String newPwd = "$2a$10$newpasswordhashabcdefghijklmno";

        // when
        adminRepository.updatePassword(id, newPwd);

        // then
        String stored = jdbcTemplate.queryForObject("SELECT password FROM admin WHERE id = ?", String.class, id);
        assertEquals(newPwd, stored);
    }

    @Test
    @DisplayName("findPasswordById 성공")
    void findPasswordByIdSuccessTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "repo_find_pwd";
        String pwd = "$2a$10$originalpasswordhashabcdefghijk";
        jdbcTemplate.update("DELETE FROM admin WHERE id = ?", id);
        String sql = "INSERT INTO admin(id, password, email, name, phonenumber, position, division, refresh_token, is_delete) VALUES(?, ?, ?, ?, ?, ?, ?, ?, false);";
        jdbcTemplate.update(sql, id, pwd, id + "@test.com", "테스터", "010-0000-0000", Role.DIVISION_MEMBER.name(), "TEST",
                "refresh" + id);

        // when
        String found = adminRepository.findPasswordById(id);

        // then
        assertEquals(pwd, found);
    }

    @Test
    @DisplayName("관리자 직책 변경 성공 - 상위권한, 동일부서")
    void updatePositionSuccessTest() {
        // given
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");

        String targetId = "target_pos_success";
        insertAdmin(targetId, Role.DIVISION_MEMBER, "TEST");
        Admin actor = buildActor("actor_pos_success", Role.PRESIDENT, "TEST");

        // when
        adminRepository.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_HEAD));

        // then
        String updated = jdbcTemplate.queryForObject("SELECT position FROM admin WHERE id = ?", String.class, targetId);
        assertEquals(Role.DIVISION_HEAD.name(), updated);
    }

    @Test
    @DisplayName("관리자 직책 변경 실패 - 동일/상위 권한 수정 불가")
    void updatePositionFailForAuthorityLevelTest() {
        // given
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");

        String targetId = "target_pos";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST");
        Admin actor = buildActor("actor_pos_fail_level", Role.DIVISION_HEAD, "TEST");

        // when - then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            adminRepository.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_MEMBER));
        });
    }

    @Test
    @DisplayName("관리자 직책 변경 실패 - 다른 부서")
    void updatePositionFailForDifferentDivisionTest() {
        // given
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "target_pos_fail_div";
        insertAdmin(targetId, Role.DIVISION_MEMBER, "TEST");
        Admin actor = buildActor("actor_pos_fail_div", Role.PRESIDENT, "OTHER");

        // when - then
        assertThrows(AccessDeniedException.class, () -> {
            adminRepository.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_HEAD));
        });
    }

    @Test
    @DisplayName("관리자 부서 변경 성공 - 상위권한, 동일부서에서 시작")
    void updateDivisionSuccessTest() {
        // given
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "target_div_success";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST");
        Admin actor = buildActor("actor_div_success", Role.PRESIDENT, "TEST");

        // when
        adminRepository.updateDivision(actor, targetId, new UpdateDivisionDto("OTHER"));

        // then
        String updated = jdbcTemplate.queryForObject("SELECT division FROM admin WHERE id = ?", String.class, targetId);
        assertEquals("OTHER", updated);
    }

    @Test
    @DisplayName("관리자 부서 변경 실패 - 동일/상위 권한 수정 불가")
    void updateDivisionFailForAuthorityLevelTest() {
        // given
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "target_div";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST");
        Admin actor = buildActor("actor_div_fail_level", Role.DIVISION_HEAD, "TEST");

        // when - then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            adminRepository.updateDivision(actor, targetId, new UpdateDivisionDto("OTHER"));
        });
    }

    @Test
    @DisplayName("관리자 삭제 후 조회 실패")
    void deleteAdminThenNotFoundTest() {
        // given
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String targetId = "target_delete";
        insertAdmin(targetId, Role.DIVISION_MEMBER, "TEST");

        // when
        adminRepository.deleteAdmin(targetId);

        // then
        assertThrows(ResourceNotFoundException.class, () -> adminRepository.findById(targetId));
    }
}
