package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.SaveItemDto;
import com.inha.borrow.backend.model.dto.item.UpdateItemDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Item객체와 관련된 DB작업을 하는 클래스
 * 
 * @author 장지왕
 */
@Repository
@AllArgsConstructor
@Slf4j
public class ItemRepository {
    private final JdbcTemplate jdbcTemplate;

    // --------- 생성 메서드 ---------

    /**
     * Item객체를 DB에 저장하는 메서드
     * 
     * @param itemDto
     * @return Item
     * @author 장지왕
     */
    public Item saveItem(SaveItemDto dto) {
        String sql = """
                INSERT INTO item(name, location, password, price)
                VALUES(?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update((connection) -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, dto.getName());
            ps.setString(2, dto.getLocation());
            ps.setString(3, dto.getPassword());
            ps.setInt(4, dto.getPrice());
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        return dto.getItem(id);
    }

    // --------- 조회 메서드 ---------
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
     * 대여물품의 상태를 반환하는 메서드
     * 
     * @param id
     * @return
     */
    public ItemState findItemStateById(Item item) {
        String sql = "SELECT state FROM item WHERE id = ?;";
        return jdbcTemplate.queryForObject(sql, (rs, index) -> {
            String state = rs.getString("state");
            return ItemState.valueOf(state);
        }, item.getId());
    }

    // --------- 수정 메서드 ---------

    /**
     * 특정 Item객체의 변경사항을 DB에 반영하는 메서드
     * 
     * @param dto 변경 내용이 담긴 UpdateItemDto
     * @param id  변경할 Item객체의 아이디
     * @throws ResourceNotFoundException 없는 자원에 대해 변경 요청을 보낸 경우
     */
    public void updateItem(Item item, UpdateItemDto dto) {
        String sql = "UPDATE item SET name = ?, location = ?, password = ?, delete_reason = ?, price = ?, state = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql,
                dto.getName(),
                dto.getLocation(),
                dto.getPassword(),
                dto.getDeleteReason(),
                dto.getPrice(),
                dto.getState().name(),
                item.getId());
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_ITEM;
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
    public void updateState(Item item, ItemState state) {
        String sql = "UPDATE item SET state = ? WHERE id =?";
        int result = jdbcTemplate.update(sql, state.name(), item.getId());
        if (result == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_ITEM;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    // --------- 삭제 메서드 ---------

    /**
     * 특정 Item객체를 DB에서 삭제하는 메서드
     * 
     * @param id               삭제할 Item의 아이디
     * @param deleteRequestDto 삭제 이유
     * @throws ResourceNotFoundException 없는 자원에 대해 삭제 요청을 보낸경우
     */
    public void deleteItem(Item item) {
        String sql = "UPDATE item SET state = 'DELETED', delete_reason = ? WHERE id = ?;";
        int affectedRow = jdbcTemplate.update(sql, item.getDeleteReason(), item.getId());
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_ITEM;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}