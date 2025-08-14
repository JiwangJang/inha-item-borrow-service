package com.inha.borrow.backend.model.dto.item;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.entity.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    @NotBlank(message = "대여물품 이름은 비울수 없습니다.")
    private String name;
    @NotBlank(message = "대여물품 위치는 비울수 없습니다.")
    private String location;
    @NotBlank(message = "대여물품의 비밀번호는 비울수 없습니다.")
    private String password;
    @Positive(message = "대여물품의 가격은 양수여야 합니다.")
    private int price;

    public Item getItem(int id) {
        return new Item(id, name, location, password, null, price, ItemState.AFFORD);
    }
}
