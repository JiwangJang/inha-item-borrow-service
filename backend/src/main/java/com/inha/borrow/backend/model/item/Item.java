package com.inha.borrow.backend.model.item;

import lombok.Data;

@Data
public class Item {
    private int id;
    private String name;
    private String location;
    private String password;
    private String deleteReason;
    private int price;
    private String state;
}
