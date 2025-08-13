package com.inha.borrow.backend.model.dto.item;

import com.inha.borrow.backend.enums.ItemState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemReviseRequestDto {
    @NotBlank(message = "대여물품 이름은 비울수 없습니다.")
    private String name;
    @NotBlank(message = "대여물품 위치는 비울수 없습니다.")
    private String location;
    @NotBlank(message = "대여물품의 비밀번호는 비울수 없습니다.")
    private String password;
    @Positive(message = "대여물품의 가격은 양수여야 합니다.")
    private int price;
    @NotNull(message = "대여물품의 상태는 NULL일수 없습니다.")
    private ItemState state;
    private String deleteReason;
}
