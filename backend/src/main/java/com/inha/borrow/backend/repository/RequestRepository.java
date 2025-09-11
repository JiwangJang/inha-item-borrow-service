package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.entity.request.RequestManager;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
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
        // Response 관련 값
        int responseId = rs.getInt("response_id");
        String rejectReason = rs.getString("reject_reason");
        Timestamp responseCreatedAt = rs.getTimestamp("response_created_at");

        // manager 관련 값
        String managerId = rs.getString("manager");
        String managerName = rs.getString("name");
        String managerPosition = rs.getString("position");

        // Request 관련 값
        int requestId = rs.getInt("request_id");
        int itemId = rs.getInt("item_id");
        String borrowerId = rs.getString("borrower_id");
        Timestamp requestCreatedAt = rs.getTimestamp("request_created_at");
        Timestamp returnAt = rs.getTimestamp("return_at");
        Timestamp borrowAt = rs.getTimestamp("borrow_at");
        RequestType type = RequestType.valueOf(rs.getString("type"));
        RequestState state = RequestState.valueOf(rs.getString("state"));
        Boolean cancel = rs.getBoolean("cancel");

        Response response = Response.builder()
                .id(responseId)
                .rejectReason(rejectReason)
                .createdAt(responseCreatedAt)
                .build();

        RequestManager manager = RequestManager.builder()
                .id(managerId)
                .name(managerName)
                .position(managerPosition)
                .build();

        return Request.builder()
                .id(requestId)
                .itemId(itemId)
                .borrowerId(borrowerId)
                .createdAt(requestCreatedAt)
                .returnAt(returnAt)
                .borrowAt(borrowAt)
                .type(type)
                .state(state)
                .cancel(cancel)
                .manager(manager)
                .response(response)
                .build();
    };

    /**
     * 리퀘스트를 저장하는 메서드
     * 
     * @param request
     * @author 형민재
     */
    @SuppressWarnings("null")
    public int save(SaveRequestDto request) {
        String sql = "INSERT INTO request(item_id, borrower_id, return_at, borrow_at, type) " +
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

    /**
     * 요청 단건조회 메서드
     * 
     * @param requestId
     * @author 형민재(수정 : 장지왕)
     */
    public Request findById(String borrowerId, int requestId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                        rq.id AS request_id,
                        rq.item_id,
                        rq.manager,
                        rq.created_at AS request_created_at,
                        rq.borrower_id,
                        rq.return_at,
                        rq.borrow_at,
                        rq.type,
                        rq.state,
                        rq.cancel,
                        rp.id AS response_id,
                        rp.create_at AS response_created_at,
                        rp.reject_reason,
                        admin.name,
                        admin.position
                    FROM request AS rq
                        LEFT JOIN response AS rp
                            ON rp.request_id = rq.id
                        LEFT JOIN admin
                            ON admin.id = rq.manager
                    WHERE request_id = ?;
                """);
        List<Object> params = new ArrayList<>();
        params.add(requestId);
        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND borrower_id =?");
            params.add(borrowerId);
        }
        try {
            Request result = jdbcTemplate.queryForObject(sql.toString(), requestRowMapper, params.toArray());
            return result;
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 조건에 따라 요청의 목록을 가져오는 메서드
     * 
     * @param borrowerId
     * @param type
     * @param state
     * @return
     * @author 형민재(수정 : 장지왕)
     */
    public List<Request> findByCondition(String borrowerId, String type, String state) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        rq.id AS request_id,
                        rq.item_id,
                        rq.manager,
                        rq.created_at AS request_created_at,
                        rq.borrower_id,
                        rq.return_at,
                        rq.borrow_at,
                        rq.type,
                        rq.state,
                        rq.cancel,
                        rp.id AS response_id,
                        rp.create_at AS response_created_at,
                        rp.reject_reason,
                        admin.name,
                        admin.position
                    FROM request AS rq
                        LEFT JOIN response AS rp
                            ON rp.request_id = rq.id
                        LEFT JOIN admin
                            ON admin.id = rq.manager
                    WHERE 1=1
                """);
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
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }

        return result;
    }

    /**
     * 사용자 요청을 전체 조회 하는 메서드
     * 
     * @author 형민재
     */
    public List<Request> findAll() {
        String sql = "SELECT * FROM request";
        List<Request> result = jdbcTemplate.query(sql, requestRowMapper);
        if (result.isEmpty()) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
        return result;
    }

    /**
     * 리퀘스트를 수정하는 메서드
     * 
     * @param patchRequestDto
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void patchRequest(PatchRequestDto patchRequestDto, int requestId, String borrowerId) {
        String sql = "UPDATE request SET return_at=?, " +
                "borrower_at=? WHERE id =? AND borrower_id=?";
        int result = jdbcTemplate.update(sql,
                patchRequestDto.getReturnAt(),
                patchRequestDto.getBorrowerAt(),
                requestId, borrowerId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트를 취소하는 메서드
     * 
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void cancelRequest(int requestId, String borrowerId) {
        String sql = "UPDATE request SET cancel=? WHERE id=? AND borrower_id=?";
        boolean cancel = true;
        int result = jdbcTemplate.update(sql, cancel, requestId, borrowerId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트 상태를 설정하는 메서드
     * 
     * @param state
     * @param requestId
     * @author 형민재
     */
    public void evaluationRequest(RequestState state, int requestId) {
        String sql = "UPDATE request SET state=? WHERE id=?";
        int result = jdbcTemplate.update(sql, state.name(), requestId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 담당자를 지정하는 메서드
     * 
     * @param adminId
     * @param requestId
     * @author 장지왕
     */
    public void manageRequest(String adminId, String requestId) {
        String sql = "UPDATE request SET state = 'ASSIGNED', manager = ? WHERE id = ?;";
        int result = jdbcTemplate.update(sql, adminId, requestId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public Request findManagerAndItemIdById(int requestId) {
        String sql = "SELECT manager, item_id, type, state FROM request WHERE id = ?;";
        try {
            Request request = jdbcTemplate.queryForObject(sql, (rs, index) -> {
                int itemId = rs.getInt("item_id");
                String managerId = rs.getString("manager");
                RequestState state = RequestState.valueOf(rs.getString("state"));
                RequestType type = RequestType.valueOf(rs.getString("type"));

                RequestManager manager = RequestManager.builder()
                        .id(managerId).build();

                return Request.builder()
                        .state(state)
                        .manager(manager)
                        .itemId(itemId)
                        .type(type)
                        .build();
            }, requestId);
            return request;
        } catch (IncorrectResultSetColumnCountException e) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 리퀘스트 삭제하는 메서드
     * 
     * @param requestId
     * @author 형민재
     */
    public void deleteRequest(int requestId) {
        String sql = "DELETE FROM request WHERE id = ?";
        int result = jdbcTemplate.update(sql, requestId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.REQUEST_NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 테스트용 메서드
     */
    public void deleteAll() {
        String sql = "DELETE FROM request";
        jdbcTemplate.update(sql);
    }
}
