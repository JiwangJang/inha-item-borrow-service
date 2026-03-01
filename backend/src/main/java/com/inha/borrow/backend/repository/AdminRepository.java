package com.inha.borrow.backend.repository;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * admin table을 다루는 클래스
 */
@Repository
@RequiredArgsConstructor
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String RAW_DEFAULT_PASSWORD = "a12345";

    /**
     * 아이디로 관리자 정보를 가져오는 메서드
     * 
     * @param id 관리자 아이디
     * @return Admin
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Admin findById(String id) {
        String sql = "SELECT * FROM admin INNER JOIN division ON division.code = admin.division WHERE admin.id = ? AND admin.is_delete = false;";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String adminId = rs.getString("id");
                String password = rs.getString("password");
                String name = rs.getString("name");
                String position = rs.getString("position");
                String divisionCode = rs.getString("code");
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
                List<GrantedAuthority> authorities = List.of(authority);
                Admin admin = Admin
                        .builder()
                        .id(adminId)
                        .password(password)
                        .name(name)
                        .authorities(authorities)
                        .position(position)
                        .divisionCode(divisionCode)
                        .build();
                return admin;
            }, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 모든 관리자의 목록을 불러오는 메서드
     * 
     * @return
     */
    public List<Admin> findAllAdmins() {
        String sql = "SELECT * FROM admin INNER JOIN division ON division.code = admin.division WHERE admin.is_delete = false;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String adminId = rs.getString("id");
            String name = rs.getString("name");
            String position = rs.getString("position");
            String divisionCode = rs.getString("code");
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
            List<GrantedAuthority> authorities = List.of(authority);
            Admin admin = Admin
                    .builder()
                    .id(adminId)
                    .password(null)
                    .name(name)
                    .authorities(authorities)
                    .position(position)
                    .divisionCode(divisionCode)
                    .build();
            return admin;
        });
    }

    /**
     * 관리자를 생성하는 메소드
     * 
     * @param saveAdminDto
     * @author 장지왕
     */
    public void saveAdmin(SaveAdminDto saveAdminDto) {
        String sql = "INSERT INTO admin(id, password, name, position, division) VALUES(?, ?, ?, ?, ?);";
        String defaultPassword = passwordEncoder.encode(RAW_DEFAULT_PASSWORD);
        try {
            jdbcTemplate.update(sql, saveAdminDto.getId(), defaultPassword,
                    saveAdminDto.getName(), saveAdminDto.getPosition().name(),
                    saveAdminDto.getDivision());
        } catch (DataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.EXIST_ID; // 에러코드 EXIST_ID로 수정
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }

    }

    /**
     * 관리자의 개인정보를 갱신하는 메서드
     * 컨트롤러에서 @AuthenticationPrincipal로 아이디를 가져와서 업데이트
     * 
     * @param id
     * @param updateAdminInfoDto
     * @author 장지왕
     */
    public void updateAdminInfo(String id, UpdateAdminInfoDto updateAdminInfoDto) {
        String sql = "UPDATE admin SET name = ?, phonenumber = ?, email = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, updateAdminInfoDto.getName(),
                updateAdminInfoDto.getPhonenumber(), updateAdminInfoDto.getEmail(),
                id);
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 비밀번호를 변경하는 메서드
     * 컨트롤러에서 @AuthenticationPrincipal로 아이디를 가져와서 업데이트
     * 
     * @param id
     * @param newPassword
     * @author 장지왕
     */
    public void updatePassword(String id, String newPassword) {
        String sql = "UPDATE admin SET password = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, newPassword, id);
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 직책을 변경하는 메서드
     * 한 단계위의 권한을 가져야만 호출 가능
     * 
     * @param admin
     * @param targetAdminId
     * @param updatePositionDto
     * @author 장지왕
     */
    @Transactional
    public void updatePosition(Admin admin, String targetAdminId, UpdatePositionDto updatePositionDto) {
        String selectSql = "SELECT position, division FROM admin WHERE id = ? AND is_delete = false;";
        String updateSql = "UPDATE admin SET position = ? WHERE id = ?;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, targetAdminId);
        if (rows.isEmpty()) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        Map<String, Object> result = rows.get(0);

        String targetPosition = (String) result.get("position");
        String targetDivision = (String) result.get("division");

        // actor(admin) vs target 권한/부서 검사
        String actorRoleName = admin.getAuthorities().stream().findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
        int actorLevel = Role.valueOf(actorRoleName).getLevel();
        int targetLevel = Role.valueOf(targetPosition).getLevel();

        if (actorLevel <= targetLevel) {
            throw new AccessDeniedException("자신보다 아래 등급만 수정할 수 있습니다.");
        }
        if (!admin.getDivisionCode().equals(targetDivision)) {
            throw new AccessDeniedException("같은 부서만 수정 가능합니다.");
        }

        int affectedRow = jdbcTemplate.update(updateSql, updatePositionDto.getPosition().name(), targetAdminId);
        if (affectedRow == 0) {
            throw new IllegalStateException("동시 수정 충돌로 인해 업데이트되지 않았습니다. 잠시 후 다시 시도해 주세요");
        }
    }

    /**
     * 부서를 변경하는 메서드
     * 한 단계위의 권한을 가져야만 호출 가능
     * 
     * @param admin             요청자(수행자)
     * @param targetAdminId     변경 대상 관리자 아이디
     * @param updateDivisionDto 변경할 부서 코드
     * @author 장지왕
     */
    @Transactional
    public void updateDivision(Admin admin, String targetAdminId, UpdateDivisionDto updateDivisionDto) {
        String selectSql = "SELECT position, division FROM admin WHERE id = ? AND is_delete = false;";
        String updateSql = "UPDATE admin SET division = ? WHERE id = ?;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, targetAdminId);
        if (rows.isEmpty()) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        Map<String, Object> result = rows.get(0);

        String targetPosition = (String) result.get("position");
        String targetDivision = (String) result.get("division");

        String actorRoleName = admin.getAuthorities().stream().findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
        int actorLevel = Role.valueOf(actorRoleName).getLevel();
        int targetLevel = Role.valueOf(targetPosition).getLevel();

        if (actorLevel <= targetLevel) {
            throw new AccessDeniedException("자신보다 아래 등급만 수정할 수 있습니다.");
        }
        if (!admin.getDivisionCode().equals(targetDivision)) {
            throw new AccessDeniedException("같은 부서만 수정 가능합니다.");
        }

        int affectedRow = jdbcTemplate.update(updateSql, updateDivisionDto.getDivision(), targetAdminId);
        if (affectedRow == 0) {
            throw new IllegalStateException("동시 수정 충돌로 인해 업데이트되지 않았습니다. 잠시 후 다시 시도해 주세요");
        }
    }

    /**
     * 관리자를 삭제하는 메서드
     * 학생회장만 이 메서드 호출가능
     * 
     * @param id
     * @author 장지왕
     */
    public void deleteAdmin(String id) {
        String sql = "UPDATE admin SET is_delete = true WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, id);
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 비밀번호 변경시 기존 비밀번호를 찾는 메서드
     * 
     * @param id
     * @return
     * @author 장지왕
     */
    public String findPasswordById(String id) {
        String sql = "SELECT password FROM admin WHERE id = ?;";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }
}
