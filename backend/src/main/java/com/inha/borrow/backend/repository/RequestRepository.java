package com.inha.borrow.backend.repository;


import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.entity.request.FindRequest;
import com.inha.borrow.backend.model.entity.request.SaveRequest;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리퀘스트 관련 리포지토리
 */
@Repository
public class RequestRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public RowMapper<FindRequest> requestRowMapper = (rs, rowNum) -> {
        int id = rs.getInt("id");
        int itemId = rs.getInt("item_id");
        String borrowerId = rs.getString("borrower_id");
        Timestamp timestampCreateAt = rs.getTimestamp("created_at");
        Timestamp timestampReturnAt = rs.getTimestamp("return_at");
        Timestamp timestampBorrowerAt = rs.getTimestamp("borrower_at");
        LocalDateTime createAt = null;
        LocalDateTime returnAt = null;
        LocalDateTime borrowerAt = null;

        if (timestampCreateAt != null) {
            createAt = timestampCreateAt.toLocalDateTime();
        }
        if (timestampReturnAt != null) {
            returnAt = timestampReturnAt.toLocalDateTime();
        }
        if (timestampBorrowerAt != null) {
            borrowerAt = timestampBorrowerAt.toLocalDateTime();
        }
        RequestType type = RequestType.valueOf(rs.getString("type"));
        RequestState state = RequestState.valueOf(rs.getString("state"));
        Boolean cancel = rs.getBoolean("cancel");
        return new FindRequest(id, itemId, borrowerId, createAt, returnAt, borrowerAt, type, state, cancel);
    };

    /**
     * 리퀘스트를 저장하는 메서드
     * @param request
     * @author 형민재
     */
    public SaveRequest save(SaveRequest request) {
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
    public FindRequest findById(int requestId) {
        try {
            String sql = "SELECT * FROM request WHERE id =?";
            return jdbcTemplate.queryForObject(sql, requestRowMapper, requestId);
        }catch (IncorrectResultSizeDataAccessException e){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public List<FindRequest> findByCondition(String borrowerId, String type, String state) {
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

        List<FindRequest> result = jdbcTemplate.query(sql.toString(), requestRowMapper, params.toArray());

        if (result.isEmpty()) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }

        return result;
    }

    /**
     * 사용자 요청을 전체 조회 하는 메서드
     * @author 형민재
     */
    public List<FindRequest> findAll(){
        String sql = "SELECT * FROM request";
        List<FindRequest> result = jdbcTemplate.query(sql,requestRowMapper);
        if(result.isEmpty()){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }return result;
    }

    /**
     * 리퀘스트를 수정하는 메서드
     * @param saveRequest
     * @param requestId
     * @author 형민재
     */
    public void patchRequest(SaveRequest saveRequest, int requestId){
        String sql = "UPDATE request SET item_id=?, borrower_id=?, return_at=?, " +
                "borrower_at=?,type=? WHERE ID =?";
        int result = jdbcTemplate.update(sql,
                saveRequest.getItemId(),
                saveRequest.getBorrowerId(),
                saveRequest.getReturnAt(),
                saveRequest.getBorrowerAt(),
                saveRequest.getType(),
                requestId);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트를 취소하는 메서드
     * @param cancel
     * @param requestId
     * @author 형민재
     */
    public void cancelRequest(boolean cancel,int requestId){
        String sql = "UPDATE request SET cancel=? WHERE ID=?";
        int result = jdbcTemplate.update(sql,cancel,requestId);
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
        int result = jdbcTemplate.update(sql,state,requestId);
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

}
