package com.inha.borrow.backend.model.entity;

import java.sql.Timestamp;
import java.time.Instant;

import com.inha.borrow.backend.enums.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    int id;
    int requestId;
    Timestamp createdAt;
    String rejectReason;
    ResponseType type;

    public Response getResponse(int id) {
        return Response.builder()
                .id(id)
                .requestId(requestId)
                .rejectReason(rejectReason)
                .type(type)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
    }
}
