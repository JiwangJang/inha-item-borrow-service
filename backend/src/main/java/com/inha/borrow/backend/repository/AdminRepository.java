package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.user.admin.DeleteAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * admin table을 다루는 클래스
 */
@Repository
@RequiredArgsConstructor
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String NOT_FOUND_MESSAGE = "존재하지 않는 관리자계정입니다.";

    /**
     * 아이디로 관리자 정보를 가져오는 메서드
     * 
     * @param id 관리자 아이디
     * @return Admin
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Admin findById(String id) {
        String sql = "SELECT * FROM admin INNER JOIN division ON division.code = admin.division WHERE admin.id = ? AND admin.is_delete != false;";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String adminId = rs.getString("id");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String name = rs.getString("name");
                String phonenumber = rs.getString("phonenumber");
                String position = rs.getString("position");
                String refreshToken = rs.getString("refresh_token");
                String divisionCode = rs.getString("code");
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
                List<GrantedAuthority> authorities = List.of(authority);
                Admin admin = Admin
                        .builder()
                        .id(adminId)
                        .password(password)
                        .email(email)
                        .name(name)
                        .phonenumber(phonenumber)
                        .authorities(authorities)
                        .refreshToken(refreshToken)
                        .divisionCode(divisionCode)
                        .build();
                return admin;
            }, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 모든 관리자의 목록을 불러오는 메서드
     * 
     * @return
     */
    public List<Admin> findAllAdmins() {
        String sql = "SELECT * FROM admin INNER JOIN division ON division.code = admin.division WHERE is_delete = false;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String adminId = rs.getString("id");
            String email = rs.getString("email");
            String name = rs.getString("name");
            String phonenumber = rs.getString("phonenumber");
            String position = rs.getString("position");
            String refreshToken = rs.getString("refresh_token");
            String divisionCode = rs.getString("code");
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
            List<GrantedAuthority> authorities = List.of(authority);
            Admin admin = Admin
                    .builder()
                    .id(adminId)
                    .password(null)
                    .email(email)
                    .name(name)
                    .phonenumber(phonenumber)
                    .authorities(authorities)
                    .refreshToken(refreshToken)
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
        String sql = "INSERT INTO admin(id, password, email, name, phonenumber, position, division) VALUES(?, ?, ?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, saveAdminDto.getId(), saveAdminDto.getPassword(), saveAdminDto.getEmail(),
                saveAdminDto.getName(), saveAdminDto.getPhonenumber(), saveAdminDto.getPosition(),
                saveAdminDto.getDivision());
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
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND;
            apiErrorCode.setMessage(NOT_FOUND_MESSAGE);
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
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND;
            apiErrorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 직책을 변경하는 메서드
     * 한 단계위의 권한을 가져야만 호출 가능
     * 
     * @param id
     * @param position
     * @author 장지왕
     */
    public void updatePosition(String id, Role position) {
        String sql = "UPDATE admin SET position = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, position, id);
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND;
            apiErrorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 부서를 변경하는 메서드
     * 한 단계위의 권한을 가져야만 호출 가능
     * 
     * @param id
     * @param division
     * @author 장지왕
     */
    public void updateDivision(String id, String division) {
        String sql = "UPDATE admin SET division = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, division, id);
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND;
            apiErrorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 관리자를 삭제하는 메서드
     * 학생회장만 이 메서드 호출가능
     * 
     * @param deleteAdminDto
     * @author 장지왕
     */
    public void deleteAdmin(DeleteAdminDto deleteAdminDto) {
        String sql = "UPDATE admin SET is_delete = true WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, deleteAdminDto.getId());
        if (affectedRow == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND;
            apiErrorCode.setMessage("존재하지 않는 관리자 계정입니다.");
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }
}
