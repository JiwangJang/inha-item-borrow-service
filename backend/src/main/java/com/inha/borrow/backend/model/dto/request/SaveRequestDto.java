package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder
public class SaveRequestDto {
    private int itemId;
    private String borrowerId;
    private Timestamp returnAt;
    private Timestamp borrowerAt;
    private RequestType type;
}

