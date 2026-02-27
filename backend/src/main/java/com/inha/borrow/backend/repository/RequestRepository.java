package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.item.RequestItem;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.entity.request.RequestManager;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        // 대응하는 요청 있는지 확인하는 부분
        int counterRequestId = rs.getInt("counter_rq_id");

        if (!(counterRequestId == 0 && state == RequestState.PERMIT)) {
            // counterRequestId == 0 : 아직 반납요청을 안했다
            // 반납요청의 경우 무조건 counterRequestId가 존재함(대여요청을 기반으로 하므로)
            // 즉, 대여요청인데 반납요청 아직 안했고 허가받은 경우에만 위치와 비밀번호 확인가능
            itemLocation = null;
            itemPassword = null;
        }

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
            ps.setTimestamp(3, Timestamp.from(request.getReturnAt().toInstant()));
            ps.setTimestamp(4, Timestamp.from(request.getBorrowAt().toInstant()));
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
        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                                rq_a.id AS request_id,
                                rq_a.item_id,
                                item.name AS item_name,
                                item.price AS item_price,
                                item.location AS item_location,
                                item.password AS item_password,
                                rq_a.created_at AS request_created_at,
                                rq_a.borrower_id,
                                borrower.name AS borrower_name,
                                rq_a.return_at,
                                rq_a.borrow_at,
                                rq_a.type,
                                rq_a.state,
                                rq_a.cancel,
                                rp.id AS response_id,
                                rp.created_at AS response_created_at,
                                rp.reject_reason,
                                rq_a.manager,
                                admin.name AS manager_name,
                                admin.position,
                                rq_b.id AS counter_rq_id
                            FROM request AS rq_a
                                -- 대여요청과 반납요청간 짝지어주는 부분
                                -- 대여시간이 동일하고 아이디와 티입이 다른 것을 묶는다
                                -- 이때 대여요청(BORROW)이면서 상태가 거절(REJECT)인 것은 제외하고 조인
                                LEFT JOIN request AS rq_b
                                    ON rq_a.borrow_at = rq_b.borrow_at
                                        AND rq_a.id != rq_b.id
                                        AND rq_a.type != rq_b.type
                                        AND NOT(rq_b.type = "BORROW" AND rq_b.state = "REJECT")
                                        AND rq_a.state != "REJECT"
                                LEFT JOIN response AS rp
                                    ON rp.request_id = rq_a.id
                                LEFT JOIN admin
                                    ON admin.id = rq_a.manager
                                LEFT JOIN item
                                    ON rq_a.item_id = item.id
                                LEFT JOIN borrower
                                    ON rq_a.borrower_id = borrower.id
                            WHERE rq_a.id = ? AND rq_a.cancel != true
                        """);
        List<Object> params = new ArrayList<>();
        params.add(requestId);
        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND rq_a.borrower_id = ?");
            params.add(borrowerId);
        }
        try {
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
    public List<Request> findRequestsByCondition(String borrowerId, String adminId, String type, String state) {
        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                                rq_a.id AS request_id,
                                rq_a.item_id,
                                item.name AS item_name,
                                item.price AS item_price,
                                item.location AS item_location,
                                item.password AS item_password,
                                rq_a.created_at AS request_created_at,
                                rq_a.borrower_id,
                                borrower.name AS borrower_name,
                                rq_a.return_at,
                                rq_a.borrow_at,
                                rq_a.type,
                                rq_a.state,
                                rq_a.cancel,
                                rp.id AS response_id,
                                rp.created_at AS response_created_at,
                                rp.reject_reason,
                                rq_a.manager,
                                admin.name AS manager_name,
                                admin.position,
                                rq_b.id AS counter_rq_id
                            FROM request AS rq_a
                                -- 대여요청과 반납요청간 짝지어주는 부분
                                -- 대여시간이 동일하고 아이디와 티입이 다른 것을 묶는다
                                -- 이때 대여요청(BORROW)이면서 상태가 거절(REJECT)인 것은 제외하고 조인
                                LEFT JOIN request AS rq_b
                                    ON rq_a.borrow_at = rq_b.borrow_at
                                        AND rq_a.id != rq_b.id
                                        AND rq_a.type != rq_b.type
                                        AND NOT(rq_b.type = "BORROW" AND rq_b.state = "REJECT")
                                        AND rq_a.state != "REJECT"
                                LEFT JOIN response AS rp
                                    ON rp.request_id = rq_a.id
                                LEFT JOIN admin
                                    ON admin.id = rq_a.manager
                                LEFT JOIN item
                                    ON rq_a.item_id = item.id
                                LEFT JOIN borrower
                                    ON rq_a.borrower_id = borrower.id
                            WHERE rq_a.cancel != true
                        """);
        List<Object> params = new ArrayList<>();

        // 관리자의 경우, 자신이 과거에 응답한 요청까지 조회할 수 있도록 수정
        if (adminId != null && !adminId.isEmpty()) {
            sql.append("AND (rq_a.manager IS NULL OR rq_a.manager = ?) ");
            params.add(adminId);
        }

        if (borrowerId != null && !borrowerId.isEmpty()) {
            sql.append("AND rq_a.borrower_id = ? ");
            params.add(borrowerId);
        }

        if (type != null && !type.isEmpty()) {
            sql.append("AND rq_a.type = ? ");
            params.add(type);
        }

        if (state != null && !state.isEmpty()) {
            sql.append("AND rq_a.state = ? ");
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
        // 반납과 대여 요청 구분

        String sql = """
                    UPDATE request SET return_at = ?, borrow_at = ? WHERE id = ? AND borrower_id = ?;
                """;
        int result = jdbcTemplate.update(sql,
                patchRequestDto.getReturnAt(),
                patchRequestDto.getBorrowAt(),
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
    public Map<String, Object> findRequestStateById(int id, RequestType type) {
        try {
            String sql = "SELECT state, borrow_at FROM request WHERE id = ? AND type = ?;";
            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                Map<String, Object> result = new HashMap<>();
                result.put("state",rs.getString("state"));
                result.put("borrowAt",rs.getTimestamp("borrow_at"));
                return result;
            }, id, type.name());
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 대여 및 반납요청의 상태, 타입, 요청시간을 조회하는 메서드(patchRequest메서드에서 사용)
     * 
     * @param id
     * @param type
     * @return
     * @author 장지왕
     */
    public Map<String, Object> findRequestStateAndTypeAndBorrowAtById(int id) {
        try {
            String sql = "SELECT state, type, borrow_at FROM request WHERE id = ?;";
            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                HashMap<String, Object> result = new HashMap<>();
                result.put("state", rs.getString("state"));
                result.put("type", rs.getString("type"));
                result.put("borrow_at", rs.getTimestamp("borrow_at"));
                return result;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 요청의 타입과 요청에 연결된 대여물품을 확인하는 메서드
     * 
     * @param id 요청 아이디
     * @return
     * @author 장지왕
     */
    public Map<String, String> findRequestItemIdAndStateById(int id) {
        try {
            String sql = """
                        SELECT
                            rq.state AS request_state,
                            it.id AS item_id
                        FROM request AS rq
                        LEFT JOIN item AS it
                        ON it.id = rq.item_id
                        WHERE rq.id = ?;
                    """;

            return jdbcTemplate.queryForObject(sql, (rs, i) -> {
                Map<String, String> result = new HashMap<>();
                result.put("item_id", rs.getString("item_id"));
                result.put("state", rs.getString("request_state"));
                return result;
            }, id);

        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public List<String> checkRequestType(String borrowerId, Timestamp borrowAt){
        String sql = "SELECT type FROM request WHERE borrower_id = ? AND borrow_at = ?";
        return jdbcTemplate.query(sql,(rs, rowNum) ->{
            String type = rs.getString("type");
            return type;
        },borrowerId,borrowAt);
    }

    public Map<String, String> checkRequestCurrentState(String borrowerId){
        try{
            String sql = "SELECT state, type FROM request WHERE borrower_id = ? ORDER BY borrower_at DESC LIMIT 1";
            return jdbcTemplate.queryForObject(sql,(rs, rowNum) -> {
                Map<String, String> result = new HashMap<>();
                result.put("state", rs.getString("state"));
                result.put("type", rs.getString("type"));
                return result;
            },borrowerId);
        }catch (EmptyResultDataAccessException e){
            return new HashMap<>();
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
