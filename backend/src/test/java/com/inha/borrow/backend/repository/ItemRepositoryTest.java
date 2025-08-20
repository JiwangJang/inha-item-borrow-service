package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
class ItemRepositoryTest {
    private final ItemRepository repository;

    @Autowired
    public ItemRepositoryTest(ItemRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
    }

    Item setUp() {
        ItemDto item = new ItemDto();
        item.setName("우산");
        item.setLocation("어딘가");
        item.setPassword("1111");
        item.setPrice(1000);
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
            log.info(e.getMessage());
        }
    }

    @Test
    @DisplayName("아이템 업데이트 객체 유효성 검사")
    public void ItemReviseRequestDtoValidationTest() {
        // given
        ItemReviseRequestDto itemReviseRequestDto = new ItemReviseRequestDto();
        itemReviseRequestDto.setName("");
        itemReviseRequestDto.setLocation("");
        itemReviseRequestDto.setDeleteReason("");
        itemReviseRequestDto.setPassword("");
        itemReviseRequestDto.setState(ItemState.AFFORD);
        // when
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemReviseRequestDto>> constraintViolations = validator.validate(itemReviseRequestDto);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("대여물품 이름은 비울수 없습니다.");
    }

    @Test
    @DisplayName("아이템 객체 유효성 검사테스트")
    public void itemDtoValidationTest() {
        // given
        ItemDto item = new ItemDto();
        item.setLocation("");
        item.setName("");
        item.setPassword("");
        item.setPrice(0);
        // when
        // then
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemDto>> constraintViolations = validator.validate(item);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("대여물품 이름은 비울수 없습니다.");
    }

    @Test
    @DisplayName("아이템 삭제 유효성 검사")
    public void itemDeleteRequestDtoValidationTest() {
        // given
        ItemDeleteRequestDto dto = new ItemDeleteRequestDto();
        dto.setDeleteReason("");
        // when
        // then
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ItemDeleteRequestDto>> constraintViolations = validator.validate(dto);
        assertThat(constraintViolations)
                .extracting(ConstraintViolation::getMessage)
                .contains("삭제이유는 비워둘 수 없습니다.");
    }

    @Test
    @DisplayName("아이템 저장")
    public void itemSaveSuccessTest() {
        // given
        // when
        Item newId = setUp();
        // then
        assertThat(newId).isInstanceOf(Item.class);
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
        Item item = setUp();
        // when
        Item founded = repository.findById(item.getId());
        // then
        assertThat(item.getId()).isEqualTo(founded.getId());
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
    @DisplayName("아이디로 삭제")
    public void deleteSuccessTest() {
        // given
        Item item = setUp();
        ItemDeleteRequestDto itemDeleteRequestDto = new ItemDeleteRequestDto();
        itemDeleteRequestDto.setDeleteReason("그냥");
        // when
        repository.deleteItem(item.getId(), itemDeleteRequestDto);
        // then
        Item deletedItem = repository.findById(item.getId());
        assertThat(deletedItem.getState()).isEqualTo(ItemState.DELETED);
    }

    @Test
    @DisplayName("아이템업데이트 테스트")
    public void itemUpdateSuccessTest() {
        // given
        Item item = setUp();
        // when
        ItemReviseRequestDto dto = new ItemReviseRequestDto();
        dto.setName("새로운 이름");
        dto.setLocation(item.getLocation());
        dto.setPassword(item.getPassword());
        dto.setPrice(item.getPrice());
        dto.setDeleteReason(null);
        dto.setState(ItemState.BORROWED);

        repository.updateItem(dto, item.getId());
        Item revisedItem = repository.findById(item.getId());
        // then
        assertThat(revisedItem.getLocation()).isEqualTo(item.getLocation());
        assertThat(revisedItem.getPassword()).isEqualTo(item.getPassword());
        assertThat(revisedItem.getPrice()).isEqualTo(item.getPrice());
        assertThat(revisedItem.getState()).isEqualTo(ItemState.BORROWED);
        assertThat(revisedItem.getName()).isEqualTo("새로운 이름");
        assertThat(revisedItem.getDeleteReason()).isEqualTo(item.getDeleteReason());
    }
}