package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BorrowerAgreementRepository {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<BorrowerAgreement> agreementRowMapper = (rs, rowNum) -> {
        int id = rs.getInt("id");
        String borrowerId = rs.getString("borrower_id");
        Timestamp agreedAt = rs.getTimestamp("agreed_at");
        String version = rs.getString("version");

        return new BorrowerAgreement(id, borrowerId, agreedAt.toLocalDateTime(), version);
    };

    /**
     * 개인정보동의를 저장하기 위한 메서드
     *
     * @param borrowerId
     * @param version
     * @author 형민재
     */
    public int saveAgreement(String borrowerId, String version) {
        String sql = "INSERT INTO borrower_privacy_agreement(borrower_id,version) VALUES(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, borrowerId);
            ps.setString(2, version);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * 개인정보동의를 불러오기 위한 메서드
     *
     * @author 형민재
     */
    public List<BorrowerAgreement> findAllAgreement() {
        String sql = "SELECT * FROM borrower_privacy_agreement";
        return jdbcTemplate.query(sql, agreementRowMapper);
    }

    /**
     * 동의한 버전으로 불러오는 메서드
     * 추후 개인정보동의 버전이 달라져 아직 동의하지 않는 인원을 불러오기 위함
     *
     * @param version
     * @author 형민재
     */
    public List<BorrowerAgreement> findbyVersion(String version) {
        String sql = "SELECT * FROM borrower_privacy_agreement WHERE version =?";
        return jdbcTemplate.query(sql, agreementRowMapper, version);
    }

    /**
     * 개인정보동의를 불러오기 위해 사용자 아이디로 불러오는 메서드
     *
     * @param borrowerId
     * @author 형민재
     */
    public List<BorrowerAgreement> findByBorrowerId(String borrowerId) {
        String sql = "SELECT * FROM borrower_privacy_agreement WHERE borrower_id = ?";

        // 1. 쿼리 실행 (데이터가 없으면 빈 리스트 반환)
        List<BorrowerAgreement> result = jdbcTemplate.query(sql, agreementRowMapper, borrowerId);

        // 2. [수정됨] 결과가 비어있으면 강제로 예외를 발생시킴
        if (result.isEmpty()) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_AGREEMENT;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        return result;
    }
}
