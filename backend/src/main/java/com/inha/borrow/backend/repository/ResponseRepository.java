package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResponseRepository {
    private final JdbcTemplate jdbcTemplate;

    @SuppressWarnings("null")
    public Response save(Response response) {
        String sql = "INSERT INTO response(request_id, reject_reason, type) VALUES(?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, response.getRequestId());
            ps.setString(2, response.getRejectReason());
            ps.setString(3, response.getType().name());
            return ps;
        }, keyHolder);

        int id = keyHolder.getKey().intValue();
        return response.getResponse(id);
    }

    public void update(String responseId, String rejectReason) {
        String sql = "UPDATE response SET reject_reason = ? WHERE id = ?;";
        int affected = jdbcTemplate.update(sql, rejectReason, responseId);
        if (affected == 0) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_RESPONSE;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }
}
