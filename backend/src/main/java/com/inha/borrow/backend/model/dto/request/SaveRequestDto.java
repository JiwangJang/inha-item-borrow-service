package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveRequestDto {
    private int prevRequestId;
    @NotNull
    private int itemId;
    @NotBlank
    private String borrowerId;
    @NotNull
    private Timestamp returnAt;
    @NotNull
    private Timestamp borrowerAt;
    @NotNull
    private RequestType type;
}
