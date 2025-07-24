package com.inha.borrow.backend.service;

import com.inha.borrow.backend.domain.Item;
import com.inha.borrow.backend.repository.ItemRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {
     ItemRepository repository;
     ItemService service;

     @BeforeEach
    public void beforeeEach(){

         HikariDataSource dataSource = new HikariDataSource();
         dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/inha_item_borrow_service");
         dataSource.setUsername("root");
         dataSource.setPassword("1234");
         dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

         repository = new ItemRepository(dataSource);
         service = new ItemService(repository);
     }
     @AfterEach
    public void afterEach(){
         repository.deleteAll();
     }

    @Test
    @DisplayName("아이템저장")
    public void 아이템저장(){
         //given
         Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = service.createItem(item);
        //when
        Item result = repository.findById(saved.getId()).get();
        //then
        assertThat(result.getId()).isEqualTo(saved.getId());
    }
    @Test
    @DisplayName("전체조회")
    public void 전체조회(){
         //given
         Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = service.createItem(item);
        //when
        List<Item> result = service.getAllItem();
        //then
        assertThat(result.size()).isEqualTo(1);
    }
    @Test
    @DisplayName("아이디로조회")
    public void 아이디로조회(){
         //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = service.createItem(item);
        //when
        Optional<Item> result = service.getItemById(saved.getId());
        //then
        assertThat(result.get().getName()).isEqualTo(saved.getName());
    }
    @Test
    @DisplayName("아이템삭제")
    public void 아이템삭제(){
         //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");
        Item saved = service.createItem(item);
        //when
        boolean result = service.deleteItem(saved.getId(),"테스트");
        //then
        assertThat(result).isTrue();
        Optional<Item> del = repository.findById(saved.getId());
        assertThat(del.get().getDeleteReason()).isEqualTo("테스트");

    }
    @Test
    @DisplayName("아이템수정")
    public void 아이템수정(){
         //given
        Item item = new Item();
        item.setName("1");
        item.setLocation("2");
        item.setPassword("3");
        item.setDeleteReason("4");
        item.setPrice(5);
        item.setState("6");

        Item item2 = new Item();
        item2.setName("qwe");
        item2.setLocation("asd");
        item2.setPassword("zxc");
        item2.setDeleteReason("5");
        item2.setPrice(5);
        item2.setState("6");

        Item saved = service.createItem(item);
        //when
        boolean result = service.updateItemDetail(item2, saved.getId());
        //then
        assertThat(result).isTrue();
        Optional<Item> update = repository.findById(saved.getId());
        assertThat(update.get().getName()).isEqualTo("qwe");
    }

}