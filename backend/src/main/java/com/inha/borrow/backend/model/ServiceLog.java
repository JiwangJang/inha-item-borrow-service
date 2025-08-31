package com.inha.borrow.backend.model;

import java.sql.Timestamp;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceLog {
    private final String errorRank;
    private final Timestamp time = Timestamp.from(Instant.now());
    private final String exceptionName;
    private final Throwable cause;
    private final String message;
    private final String userId;
    private final String authority;
    private final String requestPath;
    private final String requestMethod;
    private final String queryString;
}
