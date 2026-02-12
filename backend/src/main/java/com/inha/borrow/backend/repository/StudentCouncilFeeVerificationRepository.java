package com.inha.borrow.backend.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudentCouncilFeeVerificationRepository {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<StudentCouncilFeeVerification> rowMapper = (rs, rowNum) -> {
        String id = rs.getString("id");
        String denyReason = (String) rs.getObject("deny_reason");
        String s3Link = (String) rs.getObject("s3_link");
        Boolean verify = (Boolean) rs.getObject("verify");
        Timestamp requestAtTimestamp = rs.getTimestamp("request_at");
        Timestamp responsetAtTimestamp = rs.getTimestamp("response_at");

        StudentCouncilFeeVerification result = StudentCouncilFeeVerification
                .builder()
                .id(id)
                .denyReason(denyReason)
                .s3Link(s3Link)
                .verify(verify)
                .requestAt(requestAtTimestamp != null ? requestAtTimestamp.toLocalDateTime() : null)
                .responseAt(responsetAtTimestamp != null ? responsetAtTimestamp.toLocalDateTime() : null)
                .build();

        return result;
    };

    /**
     * 사용자가 개인정보 수집 동의했을때 저장하는 거
     * 
     * @param id
     * @author 장지왕
     */
    public void initialSave(String id) {
        String query = """
                INSERT INTO student_council_fee(id) VALUE(?); ;
                """;

        jdbcTemplate.update(query, id);
    }

    /**
     * 사용자가 학생회비 납부정보를 등록할 때 쓰는 메서드
     * 
     * @param id     사용자 아이디
     * @param s3Link 납부인증사진 저장 경로
     * @author 장지왕
     */
    public void verificationRequestSave(String id, String s3Link) {
        LocalDateTime current = LocalDateTime.now();
        String query = """
                UPDATE student_council_fee SET
                    verify = NULL,
                    s3_link = ?,
                    request_at = ?,
                    response_at = NULL,
                    deny_reason = NULL
                WHERE id = ?;
                """;

        jdbcTemplate.update(query, s3Link, current, id);
    }

    /**
     * 관리자가 처리해야할 요청과 처리한 요청을 볼때 사용하는 메서드(다건조회)
     * 
     * @return 인증 요청목록
     * @author 장지왕
     */
    public List<StudentCouncilFeeVerification> findAllRequests() {
        // request_at이 NULL이라는 것은 사용자가 학생회비 납부 인증 신청을 하지 않았음을 의미
        String query = """
                SELECT * FROM student_council_fee
                WHERE request_at IS NOT NULL;
                """;
        return jdbcTemplate.query(query, rowMapper);
    }

    /**
     * 관리자나 사용자가 단건조회할 때 사용하는 메서드
     * 
     * @param id 사용자 아이디
     * @return 인증요청 객체
     * @author 장지왕
     */
    public StudentCouncilFeeVerification findRequestById(String id) {
        String query = """
                SELECT * FROM student_council_fee
                WHERE id = ?;
                """;
        try{
            StudentCouncilFeeVerification studentCouncilFeeVerification = jdbcTemplate.queryForObject(query, rowMapper, id);
            return studentCouncilFeeVerification;
        }catch (EmptyResultDataAccessException e){
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_COUNCIL;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }

    /**
     * 관리자가 요청을 수정할때 사용하는 메서드
     * 
     * @param id         사용자 아이디
     * @param verify     승인여부
     * @param denyReason 거절이유(승인으로 고칠경우 null)
     * @author 장지왕
     */
    public void updateForAdmin(String id, boolean verify, String denyReason) {
        LocalDateTime current = LocalDateTime.now();
        String query = """
                INSERT INTO student_council_fee(verify, deny_reason, response_at) INTO (?, ?, ?)
                WHERE id = ?;
                """;

        jdbcTemplate.update(query, verify, denyReason, current, id);
    }

    /**
     * 사용자가 인증요청을 취소했을 때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void cancel(String id) {
        String query = """
                UPDATE student_council_fee SET
                    verify = NULL,
                    s3_link = NULL,
                    request_at = NULL,
                    response_at = NULL,
                    deny_reason = NULL
                WHERE id = ?;
                """;
        jdbcTemplate.update(query, id);
    }
}
