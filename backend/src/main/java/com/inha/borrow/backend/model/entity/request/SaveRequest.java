package com.inha.borrow.backend.model.entity.request;

import com.inha.borrow.backend.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class SaveRequest {
    private int itemId;
    private String borrowerId;
    private Timestamp returnAt;
    private Timestamp borrowerAt;
    private RequestType type;
}

