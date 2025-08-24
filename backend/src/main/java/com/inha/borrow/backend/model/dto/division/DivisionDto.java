package com.inha.borrow.backend.model.dto.division;

import com.inha.borrow.backend.model.entity.Division;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionDto {
    private String code;
    private String name;

    public Division getDivision() {
        return new Division(this.code, this.name);
    }
}