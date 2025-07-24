package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.domain.Item;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;


class ItemRepositoryTest {
    private ItemRepository repository;

    @BeforeEach
    void setUp() {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/inha_item_borrow_service");
        dataSource.setUsername("root");
        dataSource.setPassword("1234");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");


        repository = new ItemRepository(dataSource);

    }
    @AfterEach
    void setup(){
        repository.deleteAll();
    }

    @Test
    @DisplayName("아이템 저장")
    public void 아이템저장() {
        //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        repository.save(item);
        //when
        Item result = repository.findById(item.getId()).get();
        //then
        assertThat(item.getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("전체 조회")
    public void 전체조회(){
        //given
        Item item1 = new Item();
        item1.setName("1");
        item1.setLocation("2");
        item1.setPassword("3");
        item1.setDeleteReason("4");
        item1.setPrice(5);
        item1.setState("6");
        repository.save(item1);

        Item item2 = new Item();
        item2.setName("1");
        item2.setLocation("2");
        item2.setPassword("3");
        item2.setDeleteReason("4");
        item2.setPrice(5);
        item2.setState("6");
        repository.save(item2);
        //when
        List<Item> result = repository.findAll();
        //then
        assertThat(result.size()).isEqualTo(2);


    }
    @Test
    @DisplayName("아이디로 찾기")
    public void 아이디로찾기(){
        //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = repository.save(item);

        //when
        Optional<Item> result = repository.findById(saved.getId());
        //then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(saved);
    }
    @Test
    @DisplayName("아이디로 삭제")
    public void 아이디로삭제(){
        //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = repository.save(item);
        //when
        boolean result = repository.deleteItem(saved.getId(),"테스트");
        //then
        assertThat(result).isTrue();
        Optional<Item> del = repository.findById(saved.getId());
        assertThat(del.get().getState()).isEqualTo("delete");
        assertThat(del.get().getDeleteReason()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("아이템업데이트")
    public void 아이템업데이트(){
        //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");

        Item item2 = new Item();
        item2.setName("asd");
        item2.setLocation("qwe");
        item2.setPassword("asd");
        item2.setDeleteReason("asd");
        item2.setPrice(5);
        item2.setState("asd");

        Item saved = repository.save(item);
        //when
        boolean result = repository.updateItem(item2, saved.getId());
        //then
        assertThat(result).isTrue();
        Optional<Item> update = repository.findById(saved.getId());
        assertThat(update.get().getName()).isEqualTo("asd");
        assertThat(update.get().getLocation()).isEqualTo("qwe");
    }


}