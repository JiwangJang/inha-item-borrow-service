package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.model.Item;
import com.inha.borrow.backend.model.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ItemRepositoryTest {
    private final ItemRepository repository;

    @Autowired
    public ItemRepositoryTest(ItemRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
    }

    int setUp() {
        Item item = new Item();
        item.setName("장우산");
        item.setLocation("2층 333번 사물함");
        item.setPassword("1234");
        item.setDeleteReason("");
        item.setPrice(1000);
        item.setState("");
        return repository.save(item);
    }

    @AfterAll
    static public void recovery() {
        String database = "jdbc:mysql://localhost:3306/inha_item_borrow_service";
        String username = "root";
        String password = "fuckDruid1!";

        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(database, username, password);
            statement = connection.createStatement();
            String sql = "DELETE FROM item;";
            statement.execute(sql);
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("아이템 저장(성공)")
    public void itemSaveSuccessTest() {
        // given
        // when
        int newId = setUp();
        // then
        assertThat(newId).isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("아이템 저장(실패)")
    public void itemSaveFailedTest() {
        // given
        Item item = new Item();
        item.setName("");
        item.setLocation("");
        item.setPassword("");
        item.setDeleteReason("");
        item.setPrice(1000);
        item.setState("");
        // when
        // then
        assertThatThrownBy(() -> repository.save(item))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("전체 조회")
    public void getAllItemTest() {
        // given
        // when
        List<Item> result = repository.findAll();
        // then
        assertThat(result.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("아이디로 찾기(성공)")
    public void findByIdSuccessTest() {
        // given
        int id = setUp();
        // when
        Item item = repository.findById(id);
        // then
        assertThat(item.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("아이디로 찾기(실패)")
    public void findByIdFailedTest() {
        // given
        // when
        // then
        assertThatThrownBy(() -> repository.findById(1123)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("아이디로 삭제(성공))")
    public void deleteSuccessTest() {
        // given
        int id = setUp();
        ItemDeleteRequestDto itemDeleteRequestDto = new ItemDeleteRequestDto();
        itemDeleteRequestDto.setDeleteReason("그냥");
        // when
        repository.deleteItem(id, itemDeleteRequestDto);
        // then
        Item deletedItem = repository.findById(id);
        assertThat(deletedItem.getState()).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("아이디로 삭제(실패))")
    public void deleteFailedTest() {
        // given
        // when
        // then
        assertThatThrownBy(() -> repository.deleteItem(1234, null)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("아이템업데이트(성공)")
    public void itemUpdateSuccessTest() {
        // given
        int id = setUp();
        // when
        Item item = new Item();
        item.setLocation("1층 333번 사물함");
        item.setName("장우산");
        item.setPassword("4321");
        item.setDeleteReason("");
        item.setPrice(10000);
        item.setState("BORROWED");
        repository.updateItem(item, id);
        Item revisedItem = repository.findById(id);
        // then
        assertThat(revisedItem.getLocation()).isEqualTo("1층 333번 사물함");
        assertThat(revisedItem.getPassword()).isEqualTo("4321");
        assertThat(revisedItem.getPrice()).isEqualTo(10000);
        assertThat(revisedItem.getState()).isEqualTo("BORROWED");
        assertThat(revisedItem.getName()).isEqualTo("장우산");
        assertThat(revisedItem.getDeleteReason()).isEqualTo("");
    }

    @Test
    @DisplayName("아이템업데이트(실패)")
    public void itemUpdateFailedTest() {
        // given
        int id = setUp();
        // when
        Item item = new Item();
        item.setLocation(null);
        item.setName(null);
        item.setPassword(null);
        item.setDeleteReason("");
        item.setPrice(10000);
        item.setState("BORROWED");
        ;
        // then
        assertThatThrownBy(() -> repository.updateItem(item, id)).isInstanceOf(DataAccessException.class);
    }

}