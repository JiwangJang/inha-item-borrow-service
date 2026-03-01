package com.inha.borrow.backend.model.dto.item;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.entity.Item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowedItemDto {
    private int itemId;
    private String borrowerId;
    private String name;
    private String location;
    private String password;
    private int price;
    private ItemState state;

    public Item getAllInfoItem() {
        return Item.builder()
                .id(itemId)
                .name(name)
                .location(location)
                .password(password)
                .price(price)
                .state(state)
                .build();
    }

    public Item getParticialInfoItem() {
        return Item.builder()
                .id(itemId)
                .name(name)
                .price(price)
                .state(state)
                .build();
    }
}
