package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Response;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResponseRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String NOT_FOUND_MESSAGE = "존재하지 않는 응답입니다.";

    public Response save(SaveResponseDto dto) {
        String sql = "INSERT INTO response(request_id, reject_reason, type) VALUES(?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dto.getRequestId());
            ps.setString(2, dto.getRejectReason());
            ps.setString(3, dto.getType().name());
            return ps;
        });

        int id = keyHolder.getKey().intValue();
        return dto.getResponse(id);
    }
}
