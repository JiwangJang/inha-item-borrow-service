package com.inha.borrow.backend.model.dto.user.admin;

import com.inha.borrow.backend.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveAdminDto {
    @Pattern(regexp = "^[a-zA-Z0-9]{4,10}$", message = "아이디는 영어대소문자와 숫자로 4~10자여야 합니다.")
    private String id;
    @NotBlank
    private String name;
    @NotNull
    private Role position;
    @NotBlank
    private String phonenumber;
    @NotBlank
    private String email;
    @NotNull
    private String division;
}
