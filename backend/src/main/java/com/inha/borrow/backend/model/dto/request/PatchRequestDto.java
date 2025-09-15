package com.inha.borrow.backend.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchRequestDto {
    @NotNull
    private Timestamp returnAt;
    @NotNull
    private Timestamp borrowerAt;
}
