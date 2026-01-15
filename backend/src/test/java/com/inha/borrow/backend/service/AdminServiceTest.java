package com.inha.borrow.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@SpringBootTest
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void ensureDivision(String code, String name) {
        String sql = "INSERT INTO division(code, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name);";
        jdbcTemplate.update(sql, code, name);
    }

    private void ensureAdminRole(Role role, int level) {
        String sql = "INSERT INTO admin_role(role, level) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level);";
        jdbcTemplate.update(sql, role.name(), level);
    }

    private void insertAdmin(String id, Role role, String division, String rawPassword) {
        jdbcTemplate.update("DELETE FROM admin WHERE id = ?", id);
        String hashed = passwordEncoder.encode(rawPassword);
        String sql = "INSERT INTO admin(id, password, email, name, phonenumber, position, division, is_delete) VALUES(?, ?, ?, ?, ?, ?, ?, false);";
        jdbcTemplate.update(sql, id, hashed, id + "@test.com", "테스터", "010-0000-0000",
                role.name(), division, "rt-" + id);
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
                .divisionCode(division)
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername 성공")
    void loadUserByUsernameSuccess() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        insertAdmin("svc_find", Role.DIVISION_MEMBER, "TEST", "pw");

        Admin found = (Admin) adminService.loadUserByUsername("svc_find");
        assertEquals("svc_find", found.getId());
    }

    @Test
    @DisplayName("loadUserByUsername 실패 - 미존재")
    void loadUserByUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> adminService.loadUserByUsername("no_such"));
    }

    @Test
    @DisplayName("findAllAdmins 포함여부 확인")
    void findAllAdminsContains() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        insertAdmin("svc_all_1", Role.DIVISION_MEMBER, "TEST", "pw");
        insertAdmin("svc_all_2", Role.DIVISION_MEMBER, "TEST", "pw");

        List<Admin> all = adminService.findAllAdmins();
        long count = all.stream().filter(a -> a.getId().equals("svc_all_1") || a.getId().equals("svc_all_2")).count();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("findById 성공")
    void findByIdSuccess() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svc_find_by_id";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST", "pw");

        Admin found = adminService.findById(id);
        assertEquals(id, found.getId());
        assertEquals("TEST", found.getDivisionCode());
    }

    @Test
    @DisplayName("findById 실패 - 미존재")
    void findByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> adminService.findById("no_such"));
    }

    @Test
    @DisplayName("saveAdmin 성공")
    void saveAdminSuccess() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svc_save";
        SaveAdminDto dto = SaveAdminDto.builder()
                .id(id)
                .name("관리자")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-1234-5678")
                .email("svc@save.com")
                .division("TEST")
                .build();

        adminService.saveAdmin(dto);

        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin WHERE id = ?", Integer.class, id);
        assertEquals(1, cnt);
    }

    @Test
    @DisplayName("saveAdmin 실패 - 중복 아이디")
    void saveAdminFailForDuplicateId() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svcsave1"; // 4~10자 제약 만족

        SaveAdminDto dto = SaveAdminDto.builder()
                .id(id)
                .name("관리자")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-1234-5678")
                .email("dup@test.com")
                .division("TEST")
                .build();

        adminService.saveAdmin(dto);
        // Repository에서 DataAccessException을 InvalidValueException으로 래핑하여 반환
        assertThrows(InvalidValueException.class, () -> adminService.saveAdmin(dto));
    }

    @Test
    @DisplayName("saveAdmin 실패 - 존재하지 않는 부서 FK 위반")
    void saveAdminFailForInvalidDivision() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        String id = "svcsave2";
        SaveAdminDto dto = SaveAdminDto.builder()
                .id(id)
                .name("관리자")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-1234-5678")
                .email("fk@test.com")
                .division("NOPE") // division 테이블에 없음
                .build();

        // FK 위반 등 DataAccessException도 Repository에서 InvalidValueException으로 래핑
        assertThrows(InvalidValueException.class, () -> adminService.saveAdmin(dto));
    }

    @Test
    @DisplayName("updateAdminInfo 성공")
    void updateAdminInfoSuccess() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svc_update_info";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST", "pw");

        UpdateAdminInfoDto info = UpdateAdminInfoDto.builder()
                .name("새이름")
                .phonenumber("010-9999-9999")
                .email("new@test.com")
                .build();

        adminService.updateAdminInfo(id, info);

        var row = jdbcTemplate.queryForMap("SELECT name, phonenumber, email FROM admin WHERE id = ?", id);
        assertEquals("새이름", row.get("name"));
        assertEquals("010-9999-9999", row.get("phonenumber"));
        assertEquals("new@test.com", row.get("email"));
    }

    @Test
    @DisplayName("updateAdminInfo 실패 - 존재하지 않는 아이디")
    void updateAdminInfoFailForNotExist() {
        // given: 존재하지 않는 아이디 사용
        String notExistId = "svc_upd_info_nf"; // 20자 이하
        UpdateAdminInfoDto info = UpdateAdminInfoDto.builder()
                .name("이름")
                .phonenumber("010-1111-2222")
                .email("noone@test.com")
                .build();

        // when - then
        assertThrows(ResourceNotFoundException.class, () -> adminService.updateAdminInfo(notExistId, info));
    }

    @Test
    @DisplayName("updatePassword 성공/실패")
    void updatePasswordTests() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svc_update_pwd";
        // AdminRepository.saveAdmin은 기본 비밀번호 a12345로 저장하므로 일관되게 직접 삽입 시에도 동일값 사용
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST", "a12345");

        // 실패: 기존 비밀번호 불일치
        UpdatePasswordDto wrong = new UpdatePasswordDto();
        wrong.setOriginPassword("wrong");
        wrong.setNewPassword("newpw");
        assertThrows(InvalidValueException.class, () -> adminService.updatePassword(id, wrong));

        // 성공: 기존 비밀번호 일치
        UpdatePasswordDto correct = new UpdatePasswordDto();
        correct.setOriginPassword("a12345");
        correct.setNewPassword("b23456");
        adminService.updatePassword(id, correct);

        String stored = jdbcTemplate.queryForObject("SELECT password FROM admin WHERE id = ?", String.class, id);
        assertTrue(passwordEncoder.matches("b23456", stored));
    }

    @Test
    @DisplayName("updatePosition 성공 - 상위권한, 동일부서")
    void updatePositionSuccess() {
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");

        String targetId = "svc_pos_t_succ";
        insertAdmin(targetId, Role.DIVISION_MEMBER, "TEST", "pw");
        Admin actor = buildActor("svc_pos_actor", Role.PRESIDENT, "TEST");

        adminService.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_HEAD));
        String updatedPos = jdbcTemplate.queryForObject("SELECT position FROM admin WHERE id = ?", String.class,
                targetId);
        assertEquals(Role.DIVISION_HEAD.name(), updatedPos);
    }

    @Test
    @DisplayName("updatePosition 실패 - 동일/상위 권한 수정 불가")
    void updatePositionFailForAuthorityLevel() {
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");

        String targetId = "svc_pos_t_fail_lvl";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST", "pw");
        Admin actor = buildActor("svc_pos_a_fail_lvl", Role.DIVISION_HEAD, "TEST");

        assertThrows(AccessDeniedException.class,
                () -> adminService.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_MEMBER)));
    }

    @Test
    @DisplayName("updatePosition 실패 - 다른 부서")
    void updatePositionFailForDifferentDivision() {
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "svc_pos_t_fail_div";
        insertAdmin(targetId, Role.DIVISION_MEMBER, "TEST", "pw");
        Admin actor = buildActor("svc_pos_a_fail_div", Role.PRESIDENT, "OTHER");

        assertThrows(AccessDeniedException.class,
                () -> adminService.updatePosition(actor, targetId, new UpdatePositionDto(Role.DIVISION_HEAD)));
    }

    @Test
    @DisplayName("updateDivision 성공 - 상위권한, 동일부서에서 시작")
    void updateDivisionSuccess() {
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "svc_div_t_succ";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST", "pw");
        Admin actor = buildActor("svc_div_actor", Role.PRESIDENT, "TEST");

        adminService.updateDivision(actor, targetId, new UpdateDivisionDto("OTHER"));
        String updatedDiv = jdbcTemplate.queryForObject("SELECT division FROM admin WHERE id = ?", String.class,
                targetId);
        assertEquals("OTHER", updatedDiv);
    }

    @Test
    @DisplayName("updateDivision 실패 - 동일/상위 권한 수정 불가")
    void updateDivisionFailForAuthorityLevel() {
        ensureAdminRole(Role.DIVISION_HEAD, 2);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "svc_div_t_fail_lvl";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST", "pw");
        Admin actor = buildActor("svc_div_a_fail_lvl", Role.DIVISION_HEAD, "TEST");

        assertThrows(AccessDeniedException.class,
                () -> adminService.updateDivision(actor, targetId, new UpdateDivisionDto("OTHER")));
    }

    @Test
    @DisplayName("updateDivision 실패 - 다른 부서")
    void updateDivisionFailForDifferentDivision() {
        ensureAdminRole(Role.PRESIDENT, 4);
        ensureDivision("TEST", "테스트");
        ensureDivision("OTHER", "다른부서");

        String targetId = "svc_div_t_fail_div";
        insertAdmin(targetId, Role.DIVISION_HEAD, "TEST", "pw");
        Admin actor = buildActor("svc_div_a_fail_div", Role.PRESIDENT, "OTHER");

        assertThrows(AccessDeniedException.class,
                () -> adminService.updateDivision(actor, targetId, new UpdateDivisionDto("OTHER")));
    }

    @Test
    @DisplayName("deleteAdmin 이후 조회 실패")
    void deleteAdminThenNotFound() {
        ensureAdminRole(Role.DIVISION_MEMBER, 1);
        ensureDivision("TEST", "테스트");
        String id = "svc_delete";
        insertAdmin(id, Role.DIVISION_MEMBER, "TEST", "pw");

        adminService.deleteAdmin(id);
        assertThrows(ResourceNotFoundException.class, () -> adminService.findById(id));
    }
}
