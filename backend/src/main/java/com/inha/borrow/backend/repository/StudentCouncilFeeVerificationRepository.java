package com.inha.borrow.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StudentCouncilFeeVerificationRepository {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<StudentCouncilFeeVerification> rowMapper = (rs, rowNum) -> {
        int id = rs.getInt("student_council_fee_verification_id");
        String borrowerId = rs.getString("borrower_id");
        String borrowerName = rs.getString("borrower_name");
        String denyReason = (String) rs.getObject("deny_reason");
        String s3Link = (String) rs.getObject("s3_link");
        Boolean verify = (Boolean) rs.getObject("verify");
        LocalDateTime requestAtTimestamp = rs.getObject("request_at", LocalDateTime.class);
        LocalDateTime responsetAtTimestamp = rs.getObject("response_at", LocalDateTime.class);

        StudentCouncilFeeVerification result = StudentCouncilFeeVerification
                .builder()
                .id(id)
                .borrowerId(borrowerId)
                .borrowerName(borrowerName)
                .denyReason(denyReason)
                .s3Link(s3Link)
                .verify(verify)
                .requestAt(requestAtTimestamp != null ? requestAtTimestamp : null)
                .responseAt(responsetAtTimestamp != null ? responsetAtTimestamp : null)
                .build();

        return result;
    };

    // --------- 생성 메서드 ---------
    /**
     * 사용자가 개인정보 수집 동의했을때 저장하는 거
     * 
     * @param id
     * @author 장지왕
     */
    public void initialSave(Borrower borrower) {
        String query = """
                INSERT INTO student_council_fee(borrower_id) VALUE(?);
                """;

        jdbcTemplate.update(query, borrower.getId());
    }

    /**
     * 사용자가 학생회비 납부정보를 등록할 때 쓰는 메서드
     * 
     * @param id     사용자 아이디
     * @param s3Link 납부인증사진 저장 경로
     * @author 장지왕
     */
    public void verificationRequestSave(Borrower borrower, String s3Link) {
        LocalDateTime current = LocalDateTime.now();
        String query = """
                UPDATE student_council_fee SET
                    verify = false,
                    s3_link = ?,
                    request_at = ?,
                    response_at = NULL,
                    deny_reason = NULL
                WHERE borrower_id = ?;
                """;

        jdbcTemplate.update(query, s3Link, current, borrower.getId());
    }

    // --------- 조회 메서드 ---------

    /**
     * 관리자가 처리해야할 요청과 처리한 요청을 볼때 사용하는 메서드(다건조회)
     * 
     * @return 인증 요청목록
     * @author 장지왕
     */
    public List<StudentCouncilFeeVerification> findAll() {
        // request_at이 NULL이라는 것은 사용자가 학생회비 납부 인증 신청을 하지 않았음을 의미
        String query = """
                SELECT
                fee.id AS student_council_fee_verification_id,
                fee.borrower_id AS borrower_id,
                b.name AS borrower_name,
                fee.s3_link,
                fee.request_at,
                fee.response_at,
                fee.deny_reason,
                fee.verify
                FROM student_council_fee AS fee
                    LEFT JOIN borrower AS b ON b.id = fee.borrower_id
                WHERE request_at IS NOT NULL;
                """;
        return jdbcTemplate.query(query, rowMapper);
    }

    /**
     * 관리자나 사용자가 단건조회할 때 사용하는 메서드
     * 
     * @param id 인증요청 아이디
     * @param id 사용자 아이디
     * @return 인증요청 객체
     * @author 장지왕
     */
    public StudentCouncilFeeVerification findByBorrowerId(Borrower borrower) {
        String query = """
                SELECT
                fee.id AS student_council_fee_verification_id,
                fee.borrower_id AS borrower_id,
                b.name AS borrower_name,
                fee.s3_link,
                fee.request_at,
                fee.response_at,
                fee.deny_reason,
                fee.verify
                FROM student_council_fee AS fee
                    LEFT JOIN borrower AS b ON b.id = fee.borrower_id
                WHERE fee.borrower_id = ?;
                """;
        try {
            StudentCouncilFeeVerification studentCouncilFeeVerification = jdbcTemplate.queryForObject(query, rowMapper,
                    borrower.getId());
            return studentCouncilFeeVerification;
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_COUNCIL;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    // --------- 수정 메서드 ---------
    /**
     * 관리자가 요청을 수정할때 사용하는 메서드
     * 
     * @param id         사용자 아이디
     * @param verify     승인여부
     * @param denyReason 거절이유(승인으로 고칠경우 null)
     * @author 장지왕 (수정 : 형민재 )
     */
    public void updateForAdmin(StudentCouncilFeeVerification verification, boolean verify, String denyReason) {
        LocalDateTime current = LocalDateTime.now();
        String query = """
                UPDATE student_council_fee
                SET verify = ?, deny_reason = ?, response_at = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(query, verify, denyReason, current, verification.getId());
    }

    /**
     * 사용자가 인증요청을 취소했을 때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void updateStudentCouncilFeeVerificationCancel(Borrower borrower) {
        String query = """
                UPDATE student_council_fee SET
                    verify = false,
                    s3_link = NULL,
                    request_at = NULL,
                    response_at = NULL,
                    deny_reason = NULL
                WHERE borrower_id = ?;
                """;
        jdbcTemplate.update(query, borrower.getId());
    }
}
