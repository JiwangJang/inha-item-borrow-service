package com.inha.borrow.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
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

    }
}
