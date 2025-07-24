package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.user.Admin;

import lombok.AllArgsConstructor;

/**
 * admin table을 다루는 클래스
 */
@Repository
@AllArgsConstructor
public class AdminRepository {
    private JdbcTemplate jdbcTemplate;

    /**
     * 아이디로 관리자 정보를 가져오는 메서드
     * 
     * @param id 관리자 아이디
     * @return Admin
     * @throws DataAccessException
     * @author 장지왕
     */
    public Admin findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM admin WHERE id=?;";
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
            String adminId = resultSet.getString("id");
            String password = resultSet.getString("password");
            String email = resultSet.getString("email");
            String name = resultSet.getString("name");
            String phonenumber = resultSet.getString("phonenumber");
            String position = resultSet.getString("position");
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(position);
            List<GrantedAuthority> authorities = List.of(authority);
            return new Admin(adminId, password, email, name, phonenumber, authorities);
        }, id);
    }
}
