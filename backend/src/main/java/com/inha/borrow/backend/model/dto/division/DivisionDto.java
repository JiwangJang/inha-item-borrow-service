package com.inha.borrow.backend.model.dto.division;

import com.inha.borrow.backend.model.entity.Division;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionDto {
    @NotBlank(message = "부서코드는 필수입니다.")
    private String code;
    @NotBlank(message = "부서명은 필수입니다.")
    private String name;

    public Division getDivision() {
        return new Division(this.code, this.name);
    }
}