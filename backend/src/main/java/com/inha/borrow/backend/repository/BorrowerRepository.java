package com.inha.borrow.backend.repository;

import java.util.List;

import com.inha.borrow.backend.model.dto.BorrowerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

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
     * @throws DataAccessException
     * @author 장지왕
     */
    public Borrower findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM borrower WHERE id=?;";
        return jdbcTemplate.queryForObject(sql,borrowerRowMapper(),id);

    }

    /**
     * 대여자 리스트를 갖는 메서드
     * @return List<Borrower>
     * @author 형민재
     */
    public List<Borrower> findAll(){
        String sql = "SELECT * FROM borrower";
        return jdbcTemplate.query(sql, borrowerRowMapper());
    }

    public BorrowerDto save(BorrowerDto borrower){
        String sql = "INSERT INTO borrower(id, password, email, name, phonenumber, " +
                "student_number, account_number) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                borrower.getId(),
                borrower.getPassword(),
                borrower.getEmail(),
                borrower.getName(),
                borrower.getPhonenumber(),
                borrower.getStudentNumber(),
                borrower.getAccountNumber());
        return borrower;
    }
    /**
     * 아이디로 대여자 정보 수정 메서드
     * @param id 대여자 아이디
     * @return int
     * @throws DataAccessException
     * @author 형민재
     */
    public Borrower patchPassword(String password, String id){
        String sql ="UPDATE borrower SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql,password, id);
        return findById(id);
    }

    public Borrower patchEmail(String email, String id){
        String sql = " UPDATE borrower SET email = ? where id = ?";
        jdbcTemplate.update(sql, email, id);
        return findById(id);
    }
    public Borrower patchName(String name, String id){
        String sql = " UPDATE borrower SET name = ? where id = ?";
        jdbcTemplate.update(sql, name, id);
        return findById(id);
    }
    public Borrower patchPhoneNumber(String phoneNumber, String id) {
        String sql = " UPDATE borrower SET phonenumber = ? where id = ?";
        jdbcTemplate.update(sql, phoneNumber, id);
        return findById(id);
    }
    public Borrower patchStudentNumber(String studentNumber, String id){
        String sql = " UPDATE borrower SET student_number = ? where id = ?";
        jdbcTemplate.update(sql, studentNumber, id);
        return findById(id);
    }
    public Borrower patchAccountNumber(String AccountNumber, String id){
        String sql = " UPDATE borrower SET account_number = ? where id = ?";
        jdbcTemplate.update(sql, AccountNumber, id);
        return findById(id);
    }
    public Borrower pathcWithDrawal(int withDrawal, String id) {
        String sql = " UPDATE borrower SET withdrawal = ? where id = ?";
        jdbcTemplate.update(sql, withDrawal, id);
        return findById(id);
    }
    public Borrower patchBan(int ban, String id){
        String sql = " UPDATE borrower SET ban = ? where id = ?";
        jdbcTemplate.update(sql, ban, id);
        return findById(id);
    }
    public void delete(){
        String sql = "DELETE FROM borrower";
        jdbcTemplate.update(sql);
    }
    private RowMapper<Borrower> borrowerRowMapper() {
        return (rs, rowNum) -> {
            String id = rs.getString("id");
            String password = rs.getString("password");
            String email = rs.getString("email");
            String name = rs.getString("name");
            String phonenumber = rs.getString("phonenumber");
            String studentNumber = rs.getString("student_number");
            String accountNumber = rs.getString("account_number");
            boolean ban = rs.getBoolean("ban");

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("BORROWER"));

            return new Borrower(id, password, email, name, phonenumber, authorities, ban, studentNumber, accountNumber);
        };

    }
}
