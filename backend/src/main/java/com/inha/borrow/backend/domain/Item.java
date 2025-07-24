package com.inha.borrow.backend.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
