package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Division 객체와 관련된 DB작업을 하는 클래스
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class DivisionRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Division 목록을 조회하는 메서드
     */
    public List<Division> findAllDivisions() {
        String sql = "SELECT * FROM division WHERE is_delete = false;";
        return jdbcTemplate.query(sql, (rs, num) -> {
            String name = rs.getString("name");
            String code = rs.getString("code");
            return new Division(code, name);
        });
    }

    /**
     * Division객체를 DB에 저장하는 메서드
     */
    public Division saveDivision(DivisionDto divisionDto) {
        String sql = "INSERT INTO division(code, name) VALUE(?, ?);";
        try {
            jdbcTemplate.update(sql,
                    divisionDto.getCode(),
                    divisionDto.getName());
        } catch (DuplicateKeyException e) {
            ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
            apiErrorCode.setMessage("이미 존재하는 부서 코드입니다.");
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        return divisionDto.getDivision();
    }

    /**
     * Division이름을 수정하는 메서드
     */
    public void updateDivision(DivisionDto divisionDto) {
        String sql = "UPDATE division SET name = ? WHERE code = ?;";

        int affected = jdbcTemplate.update(sql, divisionDto.getName(), divisionDto.getCode());
        if (affected == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage("수정하려는 부서가 존재하지 않습니다.");
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * Division하나를 삭제하는 메서드
     */
    public void deleteDivision(DivisionDto divisionDto) {
        String sql = "UPDATE division SET is_delete = true WHERE code = ?;";

        int affected = jdbcTemplate.update(sql, divisionDto.getCode());
        if (affected == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage("삭제하려는 부서가 존재하지 않습니다.");
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}
