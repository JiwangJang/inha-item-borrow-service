package com.inha.borrow.backend.model.entity;

import com.inha.borrow.backend.enums.ItemState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private int id;
    private String name;
    private String location;
    private String password;
    private String deleteReason;
    private int price;
    private ItemState state;
}
