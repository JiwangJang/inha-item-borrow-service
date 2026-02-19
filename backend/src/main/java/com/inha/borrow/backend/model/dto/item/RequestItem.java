package com.inha.borrow.backend.model.dto.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RequestItem {
    private int id;
    private String name;
    private int price;
    private String location;
    private String password;
}
