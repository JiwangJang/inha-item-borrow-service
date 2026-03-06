package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
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

    // --------- 생성 메서드 ---------

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

    // --------- 조회 메서드 ---------

    /**
     * 아이디로 관리자 정보를 가져오는 메서드
     * 
     * @param id 관리자 아이디
     * @return Admin
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Admin findById(Admin admin) {
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
                Admin foundAdmin = Admin
                        .builder()
                        .id(adminId)
                        .password(password)
                        .name(name)
                        .authorities(authorities)
                        .position(position)
                        .divisionCode(divisionCode)
                        .build();
                return foundAdmin;
            }, admin.getId());
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
     * 비밀번호 변경시 기존 비밀번호를 찾는 메서드
     * 
     * @param id
     * @return
     * @author 장지왕
     */
    public String findPasswordById(Admin admin) {
        String sql = "SELECT password FROM admin WHERE id = ?;";
        return jdbcTemplate.queryForObject(sql, String.class, admin.getId());
    }

    // --------- 수정 메서드 ---------

    /**
     * 비밀번호를 변경하는 메서드
     * 컨트롤러에서 @AuthenticationPrincipal로 아이디를 가져와서 업데이트
     * 
     * @param id
     * @param newPassword
     * @author 장지왕
     */
    public void updatePassword(Admin admin, UpdatePasswordDto updatePasswordDto) {
        String sql = "UPDATE admin SET password = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, updatePasswordDto.getNewPassword(), admin.getId());
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_ADMIN;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    // --------- 삭제 메서드 ---------

    /**
     * 관리자를 삭제하는 메서드(soft-delete)
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

}
