package com.inha.borrow.backend.repository;

import java.util.List;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * borrower table을 다루는 클래스
 */
@Repository
@RequiredArgsConstructor
public class BorrowerRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Borrower> borrowerRowMapper = (rs, rowNum) -> {
        String id = rs.getString("id");
        String phonenumber = rs.getString("phonenumber");
        String name = rs.getString("name");
        String accountNumber = rs.getString("account_number");
        String department = rs.getString("department");
        boolean ban = rs.getBoolean("ban");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("BORROWER"));

        return new Borrower(id, name, phonenumber, authorities, ban, accountNumber, department);
    };

    /**
     * 아이디로 대여자 정보를 가져오는 메서드
     * 
     * @param id 대여자 아이디
     * @return Borrower
     * @throws ResourceNotFoundException 없는 아이디를 찾을 경우
     * @author 장지왕
     */
    public Borrower findById(String id) {
        try {
            String sql = "SELECT * FROM borrower WHERE id=?;";
            return jdbcTemplate.queryForObject(sql, borrowerRowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 대여자 리스트를 반환하는 메서드
     * 
     * @return List<Borrower>
     * @author 형민재
     */
    public List<Borrower> findAll() {
        String sql = "SELECT * FROM borrower";
        List<Borrower> result = jdbcTemplate.query(sql, borrowerRowMapper);
        return result;
    }

    public List<CacheBorrowerDto> findAllWithFeeVerification() {
        String sql = """
                SELECT
                    b.id,
                    b.name,
                    b.department,
                    b.phone_number,
                    b.account_number,
                    b.ban,
                    v.verify, -- TINYINT(1)은 보통 0/1로 나오므로 0 처리
                    v.s3_link
                FROM borrower b
                LEFT JOIN student_council_fee v ON b.id = v.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return CacheBorrowerDto.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .department(rs.getString("department"))
                    .phoneNumber(rs.getString("phonenumber"))
                    .accountNumber(rs.getString("account_number"))
                    .ban(rs.getBoolean("ban"))
                    .verify(rs.getBoolean("verify"))
                    .s3Link(rs.getString("s3_link"))
                    .build();
        });
    }

    public void save(BorrowerDto borrower) {
        String sql = "INSERT INTO borrower(id, name, department, phone_number, " +
                " account_number) VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                borrower.getId(),
                borrower.getName(),
                borrower.getDepartment(),
                borrower.getPhonenumber(),
                borrower.getAccountNumber());
    }

    /**
     * 아이디로 대여자 정보 수정 메서드
     * 
     * @param id 대여자 아이디
     * @return int
     * @throws DataAccessException
     * @author 형민재
     */
    public void patchName(String name, String id) {
        String sql = " UPDATE borrower SET name = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, name, id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public void patchPhoneNumber(String phoneNumber, String id) {
        String sql = " UPDATE borrower SET phonenumber = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, phoneNumber, id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            ;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }

    }

    public void patchAccountNumber(String accountNumber, String id) {
        String sql = " UPDATE borrower SET account_number = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, accountNumber, id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public void patchBan(Boolean ban, String id) {
        String sql = " UPDATE borrower SET ban = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, ban, id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * test 코드에 사용하기 위한 메서드
     */
    public void deleteAll() {
        String sql = "DELETE FROM borrower";
        jdbcTemplate.update(sql);
    }
}
