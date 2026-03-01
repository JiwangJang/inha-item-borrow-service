package com.inha.borrow.backend.model.dto.item;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDeleteRequestDto {
    @NotBlank(message = "삭제이유는 비워둘 수 없습니다.")
    private String deleteReason;
}
