package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder
public class SaveRequestDto {
    private int prevRequestId;
    @NotBlank
    private int itemId;
    @NotBlank
    private String borrowerId;
    @NotBlank
    private Timestamp returnAt;
    @NotBlank
    private Timestamp borrowerAt;
    @NotNull
    private RequestType type;
}
