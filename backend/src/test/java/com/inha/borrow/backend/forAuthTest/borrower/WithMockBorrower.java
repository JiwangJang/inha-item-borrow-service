package com.inha.borrow.backend.forAuthTest.borrower;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockBorrowerSecurityContextFactory.class)
public @interface WithMockBorrower {
    String id() default "12345678";

    String[] authorities() default { "BORROWER" };
}
