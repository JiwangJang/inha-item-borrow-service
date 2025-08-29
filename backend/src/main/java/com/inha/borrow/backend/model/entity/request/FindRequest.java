package com.inha.borrow.backend.model.entity.request;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class FindRequest {
    private int id;
    private int itemId;
    private String borrowerId;
    private LocalDateTime createdAt;
    private LocalDateTime returnAt;
    private LocalDateTime borrowerAt;
    private RequestType type;
    private RequestState state;
    private Boolean cancel;
}
