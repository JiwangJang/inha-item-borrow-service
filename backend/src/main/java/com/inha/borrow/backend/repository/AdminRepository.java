package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
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

    /**
     * 아이디로 관리자 정보를 가져오는 메서드
     * 
     * @param id 관리자 아이디
     * @return Admin
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Admin findById(String id) {
        String sql = "SELECT * FROM admin WHERE id=?;";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String adminId = rs.getString("id");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String name = rs.getString("name");
                String phonenumber = rs.getString("phonenumber");
                String position = rs.getString("position");
                String refreshToken = rs.getString("refresh_token");
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
                List<GrantedAuthority> authorities = List.of(authority);
                return new Admin(adminId, password, email, name, phonenumber, authorities, refreshToken);
            }, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}
