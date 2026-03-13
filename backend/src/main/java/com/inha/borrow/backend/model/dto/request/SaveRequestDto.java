package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveRequestDto {
    private int prevRequestId;
    @NotNull
    private int itemId;
    @NotNull
    private LocalDateTime returnAt;
    @NotNull
    private LocalDateTime borrowAt;
    @NotNull
    private RequestType type;
}
