package com.inha.borrow.backend.model.dto.division;

import com.inha.borrow.backend.model.entity.Division;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionDto {
    @NotBlank(message = "부서코드는 필수입니다.")
    @Pattern(regexp = "^[A-Z]+$", message = "부서코드는 영어 대문자만 허용됩니다.")
    private String code;
    @NotBlank(message = "부서명은 필수입니다.")
    private String name;

    public Division getDivision() {
        return new Division(this.code, this.name);
    }
}