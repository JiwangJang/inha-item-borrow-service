package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveRequestDto {
    private int prevRequestId;
    @NotNull
    private int itemId;
    @NotNull
    private OffsetDateTime returnAt;
    @NotNull
    private OffsetDateTime borrowAt;
    @NotNull
    private RequestType type;
}
