package com.inha.borrow.backend.model.dto.response;

import java.sql.Timestamp;
import java.time.Instant;

import com.inha.borrow.backend.enums.ResponseType;
import com.inha.borrow.backend.model.entity.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponseDto {
    int requestId;
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
