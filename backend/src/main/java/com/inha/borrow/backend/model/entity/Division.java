package com.inha.borrow.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Division {
    private final String code;
    private final String name;
}
