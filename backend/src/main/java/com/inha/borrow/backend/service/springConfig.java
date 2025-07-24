package com.inha.borrow.backend.service;

import com.inha.borrow.backend.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class springConfig {

    private DataSource dataSource;
    private ItemRepository itemRepository;

    @Autowired
    public springConfig(DataSource dataSource){
        this.dataSource= dataSource;
    }
    @Bean
    public ItemRepository itemRepository(){
        return new ItemRepository(dataSource);
    }

    @Bean
    public ItemService itemService(){
        return new ItemService(itemRepository());
    }


}
