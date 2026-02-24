package com.inha.borrow.backend.repository;

import java.util.List;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
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
        String phonenumber = rs.getString("phone_number");
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

    /**
     * CACHE에 저장하기 위해 대여자 리스트와 납부정보를 JOIN하여 반환하는 메서드
     *
     * @return List<CacheBorrowerDto>
     * @author 형민재
     */
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
                    v.s3_link,
                    p.version,
                    b.ban_reason
                FROM borrower b
                LEFT JOIN student_council_fee v ON b.id = v.borrower_id
                LEFT JOIN borrower_privacy_agreement p ON b.id = p.borrower_id;
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return CacheBorrowerDto.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .department(rs.getString("department"))
                    .phoneNumber(rs.getString("phone_number"))
                    .accountNumber(rs.getString("account_number"))
                    .ban(rs.getBoolean("ban"))
                    .verify(rs.getBoolean("verify"))
                    .s3Link(rs.getString("s3_link"))
                    .agreementVersion(rs.getString("version"))
                    .banReason(rs.getString("ban_reason"))
                    .build();
        });
    }

    /**
     * CACHE에 저장할 단일 대여자 객체를 가져오는 메서드
     *
     * @return List<Borrower>
     * @author 형민재
     */
    public CacheBorrowerDto findByIdWithFeeVerification(String borrowerId) {
        try {
            String sql = """
                    SELECT
                        b.id,
                        b.name,
                        b.department,
                        b.phone_number,
                        b.account_number,
                        b.ban,
                        v.verify, -- TINYINT(1)은 보통 0/1로 나오므로 0 처리
                        v.s3_link,
                        p.version,
                        b.ban_reason
                    FROM borrower b
                    LEFT JOIN student_council_fee v ON b.id = v.borrower_id
                    LEFT JOIN borrower_privacy_agreement p ON b.id = p.borrower_id
                    WHERE b.id = ?;
                        """;
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return CacheBorrowerDto.builder()
                        .id(rs.getString("id"))
                        .name(rs.getString("name"))
                        .department(rs.getString("department"))
                        .phoneNumber(rs.getString("phone_number"))
                        .accountNumber(rs.getString("account_number"))
                        .ban(rs.getBoolean("ban"))
                        .verify(rs.getBoolean("verify"))
                        .s3Link(rs.getString("s3_link"))
                        .agreementVersion(rs.getString("version"))
                        .banReason(rs.getString("ban_reason"))
                        .build();
            }, borrowerId);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
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
     * 아이디로 대여자의 전화번호 계좌번호를 저장하는 메서드
     *
     * @param id  대여자 아이디
     * @param dto
     *
     * @author 형민재
     */
    public void savePhoneAccountNumber(String id, SavePhoneAccountNumberDto dto) {
        String sql = "UPDATE borrower SET phone_number = ?, account_number =? WHERE id = ?";
        int result = jdbcTemplate.update(sql, dto.getPhoneNumber(), dto.getAccountNumber(), id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
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
        String sql = " UPDATE borrower SET phone_number = ? WHERE id = ?";
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
