package com.inha.borrow.backend.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchRequestDto {
    private Timestamp returnAt;
    private Timestamp borrowerAt;
}
