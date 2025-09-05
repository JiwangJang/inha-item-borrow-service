package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.BorrowedItemDto;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.AllArgsConstructor;

/**
 * Item객체와 관련된 DB작업을 하는 클래스
 * 
 * @author 장지왕
 */
@Repository
@AllArgsConstructor
public class ItemRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String NOT_FOUND_MESSAGE = "존재하지 않는 물품입니다.";

    /**
     * Item객체를 DB에 저장하는 메서드
     * 
     * @param itemDto
     * @return Item
     * @author 장지왕
     */
    @SuppressWarnings("null")
    public Item save(ItemDto itemDto) {
        String sql = """
                INSERT INTO item(name, location, password, price)
                VALUES(?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update((connection) -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, itemDto.getName());
            ps.setString(2, itemDto.getLocation());
            ps.setString(3, itemDto.getPassword());
            ps.setInt(4, itemDto.getPrice());
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        return itemDto.getItem(id);
    }

    /**
     * 모든 Item 정보를 DB에서 가져오는 메서드(관리자만 호출가능)
     * 
     * @return List<Item>
     * @author 장지왕
     */
    public List<Item> findAllForAdmin() {
        String sql = "SELECT * FROM item WHERE state != 'DELETED' ORDER BY id DESC;";
        return jdbcTemplate.query(sql, (ResultSet resultSet, int index) -> Item.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .location(resultSet.getString("location"))
                .password(resultSet.getString("password"))
                .price(resultSet.getInt("price"))
                .state(ItemState.valueOf(resultSet.getString("state")))
                .deleteReason(resultSet.getString("delete_reason"))
                .build());
    }

    /**
     * 필터링된 Item 정보를 DB에서 가져오는 메서드
     * <p>
     * 아이템 아이디, 이름, 상태, 가격정보만 가져온다
     * 
     * @return List<Item>(비밀번호와 위치 정보는 없다)
     * @author 장지왕
     */
    public List<Item> findAllForNotAdmin() {
        String sql = "SELECT id, name, price, state FROM item WHERE state != 'DELETED' ORDER BY id DESC;";
        return jdbcTemplate.query(sql, (ResultSet resultSet, int index) -> Item.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .price(resultSet.getInt("price"))
                .state(ItemState.valueOf(resultSet.getString("state")))
                .build());
    }

    /**
     * 특정 Item을 DB에서 찾는 메서드
     * <p>
     * request 테이블과 조인해서 해당 대여물품에 대한 대여자가 있는지 확인
     * 
     * @param id
     * @return Item
     * @author 장지왕
     * @throws ResourceNotFoundException 없는 자원에 대해 조회 요청을 보낸경우
     */
    public BorrowedItemDto findById(int id) {
        try {
            String sql = """
                        SELECT request.borrower_id, itme.id, item.name, item.location, item.password, item.delete_reason, item.state
                        FROM item LEFT JOIN request ON item.id = request.item_id
                        WHERE item.id = ? AND request.state = 'PERMIT' AND request.cancel = false AND item.state != DELETED;
                    """;
            return jdbcTemplate.queryForObject(sql, (ResultSet resultSet, int index) -> {
                return BorrowedItemDto.builder()
                        .itemId(resultSet.getInt("id"))
                        .borrowerId(resultSet.getString("borrower_id"))
                        .name(resultSet.getString("name"))
                        .location(resultSet.getString("location"))
                        .password(resultSet.getString("password"))
                        .price(resultSet.getInt("price"))
                        .state(ItemState.valueOf(resultSet.getString("state")))
                        .build();
            }, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 특정 Item객체를 DB에서 삭제하는 메서드
     * 
     * @param id           삭제할 Item의 아이디
     * @param deleteReason 삭제 이유
     * @throws ResourceNotFoundException 없는 자원에 대해 삭제 요청을 보낸경우
     */
    public void deleteItem(int id, ItemDeleteRequestDto deleteRequestDto) {
        String sql = "UPDATE item SET state = 'DELETED', delete_reason = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, deleteRequestDto.getDeleteReason(), id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * 특정 Item객체의 변경사항을 DB에 반영하는 메서드
     * 
     * @param item 변경 내용이 담긴 Item
     * @param id   변경할 Item객체의 아이디
     * @throws ResourceNotFoundException 없는 자원에 대해 변경 요청을 보낸 경우
     */
    public void updateItem(ItemReviseRequestDto item, int id) {
        String sql = "UPDATE item SET name = ?, location = ?, password = ?, delete_reason = ?, price = ?, state = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, item.getName(), item.getLocation(), item.getPassword(),
                item.getDeleteReason(),
                item.getPrice(), item.getState().name(), id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage(NOT_FOUND_MESSAGE);
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * item state를 변경하는 메서드
     *
     * @param state
     * @param id
     * @throws ResourceNotFoundException 없는 자원에 대해 변경 요청을 보낸 경우
     * @author 형민재
     */
    public void updateState(ItemState state, int id) {
        String sql = "UPDATE item SET state = ? WHERE id =?";
        int result = jdbcTemplate.update(sql, state.name(), id);
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM item";
        jdbcTemplate.update(sql);
    }
}
