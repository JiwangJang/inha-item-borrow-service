package com.inha.borrow.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.TempBorrowerInfoDto;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 기존 사용자 캐쉬
     *
     * @author 형민재
     */
    @Bean
    public Cache<String, CacheBorrowerDto> borrowerCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }

    /**
     * 신규 사용자를 위한 임시 캐쉬
     *
     * @author 형민재
     */
    @Bean
    public Cache<String, TempBorrowerInfoDto> tempBorrowerCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .build();
    }
}
