package com.inha.borrow.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationFilter;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationFilter;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.service.AdminService;

@Configuration
@EnableWebSecurity(debug = true)
public class AuthConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager)
            throws Exception {
        AdminAuthenticationFilter adminAuthenticationFilter = new AdminAuthenticationFilter(authenticationManager);
        BorrowerAuthenticationFilter borrowerAuthenticationFilter = new BorrowerAuthenticationFilter(
                authenticationManager);

        httpSecurity
                .csrf((csrf) -> csrf.disable())
                .formLogin((form) -> form.disable())
                .authorizeHttpRequests((authorize) -> {
                    authorize.anyRequest().permitAll();
                })
                .addFilterAt(adminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(borrowerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AdminService adminService,
            AdminAuthenticationProvider adminAuthenticationProvider,
            BorrowerAuthenticationProvider borrowerAuthenticationProvider) {

        return new ProviderManager(List.of(adminAuthenticationProvider, borrowerAuthenticationProvider));
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
