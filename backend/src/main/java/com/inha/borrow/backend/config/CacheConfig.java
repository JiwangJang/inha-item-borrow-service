package com.inha.borrow.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.StudentCouncilFee;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public Cache<Borrower, StudentCouncilFee> borrowerCache(){
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }
}
