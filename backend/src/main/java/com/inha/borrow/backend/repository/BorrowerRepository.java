package com.inha.borrow.backend.repository;

import java.util.List;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.dto.user.borrower.SaveBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.UpdateAccountNumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.UpdateBanDto;
import com.inha.borrow.backend.model.dto.user.borrower.UpdatePhonenumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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

    // --------- 생성 메서드 ---------
    /**
     * 대여자 정보를 저장하는 메서드
     * 
     * @param saveBorrowerDto
     */
    public void save(SaveBorrowerDto saveBorrowerDto) {
        String sql = "INSERT INTO borrower(id, name, department, phone_number, " +
                " account_number) VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                saveBorrowerDto.getId(),
                saveBorrowerDto.getName(),
                saveBorrowerDto.getDepartment(),
                saveBorrowerDto.getPhonenumber(),
                saveBorrowerDto.getAccountNumber());
    }

    // --------- 생성 메서드 ---------

    /**
     * CACHE에 저장하기 위해 대여자 리스트와 납부정보를 JOIN하여 반환하는 메서드
     *
     * @return List<BorrowerCacheData>
     * @author 형민재
     */
    public List<BorrowerCacheData> findAllForCache() {
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
            return BorrowerCacheData.builder()
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
     * @return BorrowerCacheData
     * @author 형민재
     */
    public BorrowerCacheData findByIdForCache(Borrower borrower) {
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
                return BorrowerCacheData.builder()
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
            }, borrower.getId());
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    // --------- 수정 메서드 ---------

    /**
     * 핸드폰 번호 수정하는 메서드
     * 
     * @param Borrower
     * @param UpdatePhonenumberDto
     */
    public void updatePhoneNumber(Borrower borrower, UpdatePhonenumberDto dto) {
        String sql = "UPDATE borrower SET phone_number = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, dto.getNewPhonenumber(), borrower.getId());
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }

    }

    /**
     * 계좌번호 수정하는 메서드
     * 
     * @param Borrower
     * @param UpdateAccountNumberDto
     */
    public void updateAccountNumber(Borrower borrower, UpdateAccountNumberDto dto) {
        String sql = "UPDATE borrower SET account_number = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, dto.getNewAccountNumber(), borrower.getId());
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 이용금지 정보 수정하는 메서드
     * 
     * @param Borrower
     * @param UpdateBandto
     */
    public void updateBan(Borrower borrower, UpdateBanDto dto) {
        String sql = "UPDATE borrower SET ban = ?, ban_reason = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, dto.isBan(), dto.getBanReason(), borrower.getId());
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_BORROWER;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}
