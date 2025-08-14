package com.inha.borrow.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
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

    private final RowMapper<Item> itemRowMapper = (ResultSet resultSet, int index) -> {
        Item item = new Item();
        item.setId(resultSet.getInt("id"));
        item.setName(resultSet.getString("name"));
        item.setLocation(resultSet.getString("location"));
        item.setPassword(resultSet.getString("password"));
        item.setPrice(resultSet.getInt("price"));
        item.setState(ItemState.valueOf(resultSet.getString("state")));
        item.setDeleteReason(resultSet.getString("delete_reason"));
        return item;
    };

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
     * 모든 Item객체를 DB에서 가져오는 메서드
     * 
     * @return Item객체 목록
     * @author 장지왕
     */
    public List<Item> findAll() {
        String sql = "SELECT * FROM item ORDER BY id DESC;";
        return jdbcTemplate.query(sql, itemRowMapper);
    }

    /**
     * 특정 Item을 DB에서 찾는 메서드
     * 
     * @param id
     * @return Item
     * @author 장지왕
     * @throws ResourceNotFoundException 없는 자원에 대해 조회 요청을 보낸경우
     */
    public Item findById(int id) {
        try {
            String sql = "SELECT * FROM item WHERE id = ?;";
            return jdbcTemplate.queryForObject(sql, itemRowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
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
                item.getPrice(), item.getState(), id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}
