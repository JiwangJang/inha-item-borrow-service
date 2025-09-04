package com.inha.borrow.backend.repository;


import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 리퀘스트 관련 리포지토리
 */
@Repository
public class RequestRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public RowMapper<Request> requestRowMapper = (rs, rowNum) -> {
        int id = rs.getInt("id");
        int itemId = rs.getInt("item_id");
        String borrowerId = rs.getString("borrower_id");
        Timestamp createAt = rs.getTimestamp("created_at");
        Timestamp returnAt = rs.getTimestamp("return_at");
        Timestamp borrowerAt = rs.getTimestamp("borrower_at");
        RequestType type = RequestType.valueOf(rs.getString("type"));
        RequestState state = RequestState.valueOf(rs.getString("state"));
        Boolean cancel = rs.getBoolean("cancel");
        return new Request(id, itemId, borrowerId, createAt, returnAt, borrowerAt, type, state, cancel);
    };

    /**
     * 리퀘스트를 저장하는 메서드
     * @param request
     * @author 형민재
     */
    public SaveRequestDto save(SaveRequestDto request) {
        String sql = "INSERT INTO request(item_id, borrower_id, return_at, borrower_at, type) " +
                "VALUES (?, ?, ?, ?, ?)";
          jdbcTemplate.update(sql,
                request.getItemId(),
                request.getBorrowerId(),
                request.getReturnAt(),
                request.getBorrowerAt(),
                request.getType().name());
          return request;
    }

    /**
     * ID로 리퀘스트를 가져오는 메서드
     * @param requestId
     * @author 형민재
     */
    public Request findById(int requestId) {
        try {
            String sql = "SELECT * FROM request WHERE id =?";
            return jdbcTemplate.queryForObject(sql, requestRowMapper, requestId);
        }catch (IncorrectResultSizeDataAccessException e){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public List<Request> findByCondition(String borrowerId, String type, String state) {
        StringBuilder sql = new StringBuilder("SELECT * FROM request WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND borrower_id = ? ");
            params.add(borrowerId);
        }

        if (type != null && !type.isEmpty()) {
            sql.append("AND type = ? ");
            params.add(type);
        }

        if (state != null && !state.isEmpty()) {
            sql.append("AND state = ? ");
            params.add(state);
        }

        List<Request> result = jdbcTemplate.query(sql.toString(), requestRowMapper, params.toArray());

        if (result.isEmpty()) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }

        return result;
    }

    /**
     * 사용자가 자신이 요청한 리퀘스트르 조회하는 메서드
     * @param borrowerId
     * @author 형민재
     */
    public List<Request> findRequestUser(String borrowerId){
        String sql = "SELECT * FROM request WHERE borrower_id = ?";
        List<Request> result =  jdbcTemplate.query(sql,requestRowMapper,borrowerId);
        if(result.isEmpty()){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
        return result;
    }

    /**
     * 사용자 요청을 전체 조회 하는 메서드
     * @author 형민재
     */
    public List<Request> findAll(){
        String sql = "SELECT * FROM request";
        List<Request> result = jdbcTemplate.query(sql,requestRowMapper);
        if(result.isEmpty()){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }return result;
    }

    /**
     * 리퀘스트를 수정하는 메서드
     * @param patchRequestDto
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void patchRequest(PatchRequestDto patchRequestDto, int requestId, String borrowerId){
        String sql = "UPDATE request SET return_at=?, " +
                "borrower_at=?,type=? WHERE ID =? AND borrower_id=?";
        int result = jdbcTemplate.update(sql,
                patchRequestDto.getReturnAt(),
                patchRequestDto.getBorrowerAt(),
                patchRequestDto.getType().name(),
                requestId,borrowerId);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트를 취소하는 메서드
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void cancelRequest(int requestId, String borrowerId){
        String sql = "UPDATE request SET cancel=? WHERE ID=? AND borrower_id=?";
        boolean cancel = true;
        int result = jdbcTemplate.update(sql,cancel,requestId,borrowerId);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트 상태를 설정하는 메서드
     * @param state
     * @param requestId
     * @author 형민재
     */
    public void evaluationRequest(RequestState state, int requestId){
        String sql = "UPDATE request SET state=? WHERE ID=?";
        int result = jdbcTemplate.update(sql,state.name(),requestId);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트 삭제하는 메서드
     * @param requestId
     * @author 형민재
     */
    public void deleteRequest(int requestId){
        String sql = "DELETE FROM request WHERE id = ?";
        int result = jdbcTemplate.update(sql,requestId);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 테스트용 메서드
     */
    public void deleteAll(){
        String sql = "DELETE FROM request";
        jdbcTemplate.update(sql);
    }


    public int saveAndReturnId(SaveRequestDto request) {
        String sql = "INSERT INTO request(item_id, borrower_id, return_at, borrower_at, type) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.getItemId());
            ps.setString(2, request.getBorrowerId());
            ps.setTimestamp(3, request.getReturnAt());
            ps.setTimestamp(4, request.getBorrowerAt());
            ps.setString(5, request.getType().name());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

}
