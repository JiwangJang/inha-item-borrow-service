package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.user.Borrower;

import lombok.AllArgsConstructor;

/**
 * borrower table을 다루는 클래스
 */
@Repository
@AllArgsConstructor
public class BorrowerRepository {
    private JdbcTemplate jdbcTemplate;

    /**
     * 아이디로 대여자 정보를 가져오는 메서드
     * 
     * @param id 대여자 아이디
     * @return Borrower
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Borrower findById(String id) {
        String sql = "SELECT * FROM borrower WHERE id=?;";
        try {
            return jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
                String adminId = resultSet.getString("id");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String phonenumber = resultSet.getString("phonenumber");
                String studentNumber = resultSet.getString("student_number");
                String accountNumber = resultSet.getString("account_number");
                boolean ban = resultSet.getBoolean("ban");

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("USER");
                List<GrantedAuthority> authorities = List.of(authority);

                return new Borrower(adminId, password, email, name, phonenumber, authorities, ban, studentNumber,
                        accountNumber);
            }, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new ResourceNotFoundException();
        }
    }
}
