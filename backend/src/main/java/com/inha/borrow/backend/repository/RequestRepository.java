package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.RequestItem;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.entity.request.RequestManager;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 리퀘스트 관련 리포지토리
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RequestRepository {
    private final JdbcTemplate jdbcTemplate;

    public RowMapper<Request> requestRowMapper = (rs, rowNum) -> {
        // Response 관련 값
        int responseId = rs.getInt("response_id");
        String rejectReason = rs.getString("reject_reason");
        Timestamp responseCreatedAt = rs.getTimestamp("response_created_at");

        log.info("responseId : {}", responseId);

        // manager 관련 값
        String managerId = rs.getString("manager");
        String managerName = rs.getString("manager_name");
        String managerPosition = rs.getString("position");

        // item 관련 값
        int itemId = rs.getInt("item_id");
        int itemPrice = rs.getInt("item_price");
        String itemName = rs.getString("item_name");
        String itemLocation = rs.getString("item_location");
        String itemPassword = rs.getString("item_password");

        // Request 관련 값
        int requestId = rs.getInt("request_id");
        String borrowerId = rs.getString("borrower_id");
        String borrowerName = rs.getString("borrower_name");
        Timestamp requestCreatedAt = rs.getTimestamp("request_created_at");
        Timestamp returnAt = rs.getTimestamp("return_at");
        Timestamp borrowAt = rs.getTimestamp("borrow_at");
        RequestType type = RequestType.valueOf(rs.getString("type"));
        RequestState state = RequestState.valueOf(rs.getString("state"));
        Boolean cancel = rs.getBoolean("cancel");

        // 여기서 response없고 request PERMIT인 경우 비밀번호랑 위치 알려줌 아니면 안알려줌

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

        RequestItem requestItem = RequestItem.builder()
                .id(itemId)
                .price(itemPrice)
                .name(itemName)
                .location(itemLocation)
                .password(itemPassword)
                .build();

        return Request.builder()
                .id(requestId)
                .item(requestItem)
                .borrowerId(borrowerId)
                .borrowerName(borrowerName)
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
    public SaveRequestResultDto save(SaveRequestDto request) {
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

        int requestId = keyHolder.getKey().intValue();
        Timestamp createdAt = Timestamp.from(Instant.now());

        return new SaveRequestResultDto(requestId, createdAt);
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
                        item.name AS item_name,
                        item.price AS item_price,
                        item.location AS item_location,
                        item.password AS item_password,
                        rq.created_at AS request_created_at,
                        rq.borrower_id,
                        borrower.name AS borrower_name,
                        rq.return_at,
                        rq.borrow_at,
                        rq.type,
                        rq.state,
                        rq.cancel,
                        rp.id AS response_id,
                        rp.created_at AS response_created_at,
                        rp.reject_reason,
                        rq.manager,
                        admin.name AS manager_name,
                        admin.position
                    FROM request AS rq
                        LEFT JOIN response AS rp
                            ON rp.request_id = rq.id
                        LEFT JOIN admin
                            ON admin.id = rq.manager
                        LEFT JOIN item
                            ON rq.item_id = item.id
                        LEFT JOIN borrower
                            ON rq.borrower_id = borrower.id
                    WHERE rq.id = ? AND rq.cancel != true
                """);
        List<Object> params = new ArrayList<>();
        params.add(requestId);
        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND rq.borrower_id = ?");
            params.add(borrowerId);
        }
        try {
            log.info("test {}", sql.toString());
            Request result = jdbcTemplate.queryForObject(sql.toString(), requestRowMapper, params.toArray());
            return result;
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
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
    public List<Request> findRequestsByCondition(String borrowerId, String type, String state) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                        rq.id AS request_id,
                        rq.item_id,
                        item.name AS item_name,
                        item.price AS item_price,
                        item.location AS item_location,
                        item.password AS item_password,
                        rq.created_at AS request_created_at,
                        rq.borrower_id,
                        borrower.name AS borrower_name,
                        rq.return_at,
                        rq.borrow_at,
                        rq.type,
                        rq.state,
                        rq.cancel,
                        rp.id AS response_id,
                        rp.created_at AS response_created_at,
                        rp.reject_reason,
                        rq.manager,
                        admin.name AS manager_name,
                        admin.position
                    FROM request AS rq
                        LEFT JOIN response AS rp
                            ON rp.request_id = rq.id
                        LEFT JOIN admin
                            ON admin.id = rq.manager
                        LEFT JOIN item
                            ON rq.item_id = item.id
                        LEFT JOIN borrower
                            ON rq.borrower_id = borrower.id
                    WHERE rq.cancel != true
                """);
        List<Object> params = new ArrayList<>();

        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND rq.borrower_id = ? ");
            params.add(borrowerId);
        }

        if (type != null && !type.isEmpty()) {
            sql.append("AND rq.type = ? ");
            params.add(type);
        }

        if (state != null && !state.isEmpty()) {
            sql.append("AND rq.state = ? ");
            params.add(state);
        }

        List<Request> result = jdbcTemplate.query(sql.toString(), requestRowMapper, params.toArray());

        if (result.isEmpty()) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
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
        String sql = """
                    UPDATE request SET return_at = ?, borrow_at = ? WHERE id = ? AND borrower_id = ?;
                """;
        int result = jdbcTemplate.update(sql,
                patchRequestDto.getReturnAt(),
                patchRequestDto.getBorrowerAt(),
                requestId, borrowerId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
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
        String sql = "UPDATE request SET cancel = ? WHERE id = ? AND borrower_id = ?;";
        boolean cancel = true;
        int result = jdbcTemplate.update(sql, cancel, requestId, borrowerId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
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
    public void manageRequest(String adminId, int requestId) {
        String sql = "UPDATE request SET state = 'ASSIGNED', manager = ? WHERE id = ?;";
        int result = jdbcTemplate.update(sql, adminId, requestId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 요청의 담당자와 물품아이디를 찾는 메서드
     * 
     * @param requestId
     * @return
     * @author 장지왕
     */
    public Request findManagerAndItemIdById(int requestId) {
        String sql = "SELECT manager, item_id, type, state FROM request WHERE id = ?;";
        try {
            Request request = jdbcTemplate.queryForObject(sql, (rs, index) -> {
                int itemId = rs.getInt("item_id");
                String managerId = rs.getString("manager");
                RequestState state = RequestState.valueOf(rs.getString("state"));
                RequestType type = RequestType.valueOf(rs.getString("type"));

                RequestManager manager = RequestManager.builder()
                        .id(managerId)
                        .build();

                RequestItem requestItem = RequestItem.builder()
                        .id(itemId)
                        .build();

                return Request.builder()
                        .state(state)
                        .item(requestItem)
                        .manager(manager)
                        .type(type)
                        .build();
            }, requestId);
            return request;
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 대여요청의 상태를 조회하는 메서드
     * 
     * @param id
     * @return
     * @author 장지왕
     */
    public RequestState findRequestStateById(int id) {
        try {
            String sql = "SELECT state FROM request WHERE id = ?;";
            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                return RequestState.valueOf(rs.getString("state"));
            }, id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 대여 및 반납요청의 상태를 조회하는 메서드
     * 
     * @param id
     * @param type
     * @return
     * @author 장지왕
     */
    public RequestState findRequestStateById(int id, RequestType type) {
        try {
            String sql = "SELECT state FROM request WHERE id = ? AND type = ?;";
            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                return RequestState.valueOf(rs.getString("state"));
            }, id, type.name());
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 대여요청인지 반납요청인지 확인하는 메서드
     * 
     * @param id
     * @return
     * @author 장지왕
     */
    public RequestType findRequestTypeById(int id) {
        try {
            String sql = "SELECT type FROM request WHERE id = ?;";
            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                return RequestType.valueOf(rs.getString("type"));
            }, id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
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

    /**
     * 대여 및 반납요청의 상태를 수정하는 메서드
     * 
     * @param state
     * @param requestId
     * @author 장지왕
     */
    public void updateRequestState(RequestState state, int requestId) {
        String sql = "UPDATE request SET state=? WHERE id=?";
        int result = jdbcTemplate.update(sql, state.name(), requestId);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}
