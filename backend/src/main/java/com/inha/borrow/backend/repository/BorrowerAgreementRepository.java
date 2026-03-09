package com.inha.borrow.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@RequiredArgsConstructor
public class BorrowerAgreementRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 개인정보동의 여부를 저장하기 위한 메서드
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
}
