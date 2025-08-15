package com.inha.borrow.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationFilter;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationFilter;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.enums.Role;

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
                    authorize
                            .requestMatchers("/borrowers").hasAuthority(Role.DIVISION_HEAD.name())
                            .requestMatchers("/items/**", "/borrowers/{borrower-id}/info/ban")
                            .hasAuthority(Role.DIVISION_MEMBER.name())
                            .requestMatchers("/borrowers/auth/**", "/admins/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/borrowers/signup-requests").permitAll()
                            .requestMatchers(HttpMethod.PATCH, "/borrowers/signup-requests/{signup-request-id}")
                            .hasAuthority(Role.DIVISION_MEMBER.name())
                            .requestMatchers("/borrowers/signup-requests/{signup-request-id}")
                            .access(new WebExpressionAuthorizationManager(
                                    "hasAuthority('BORROWER') && #signup-request-id == authentication.id"))
                            .requestMatchers("/borrowers/info/**").hasAuthority(Role.BORROWER.name())
                            .anyRequest().authenticated();

                })
                .addFilterAt(adminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(borrowerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AdminAuthenticationProvider adminAuthenticationProvider,
            BorrowerAuthenticationProvider borrowerAuthenticationProvider) {

        return new ProviderManager(List.of(adminAuthenticationProvider, borrowerAuthenticationProvider));
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.PRESIDENT.name()).implies(Role.VICE_PRESIDENT.name())
                .role(Role.DIVISION_HEAD.name()).implies(Role.DIVISION_MEMBER.name())
                .build();
    }

}
