package com.inha.borrow.backend.forAuthTest.admin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.inha.borrow.backend.enums.Role;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
public @interface WithMockAdmin {
    String id() default "test_admin";

    Role role() default Role.DIVISION_MEMBER;

    String division() default "TEST";
}
