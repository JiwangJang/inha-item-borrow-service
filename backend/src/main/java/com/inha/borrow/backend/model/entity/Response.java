package com.inha.borrow.backend.model.entity;

import java.sql.Timestamp;

import com.inha.borrow.backend.enums.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    int id;
    int requestId;
    Timestamp createdAt;
    String rejectReason;
    ResponseType type;
}
