package com.inha.borrow.backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Configuration
public class Config {
    @Value("sms-service.api-key")
    private String smsServiceApiKey;
    @Value("sms-service.secret-api-key")
    private String smsServiceSecretApiKey;

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    DefaultMessageService messageService() {
        return NurigoApp.INSTANCE.initialize(smsServiceApiKey, smsServiceSecretApiKey, "https://api.solapi.com");
    }
}
