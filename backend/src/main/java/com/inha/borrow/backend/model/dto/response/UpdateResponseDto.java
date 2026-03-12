package com.inha.borrow.backend.model.dto.response;

import com.inha.borrow.backend.enums.RequestState;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResponseDto {
    @NotNull
    private int requestId;
    private RequestState requestState;
    private String rejectReason;
}
