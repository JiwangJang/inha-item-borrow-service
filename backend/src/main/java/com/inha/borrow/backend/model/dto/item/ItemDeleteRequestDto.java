package com.inha.borrow.backend.model.dto.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemDeleteRequestDto {
    @NotBlank(message = "삭제이유는 비워둘 수 없습니다.")
    private String deleteReason;
}
