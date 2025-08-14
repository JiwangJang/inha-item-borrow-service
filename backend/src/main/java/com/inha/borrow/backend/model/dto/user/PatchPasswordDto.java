package com.inha.borrow.backend.model.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatchPasswordDto {
    private String originPassword;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[!@#$%^&*()_\\\\-+=])[A-Za-z\\\\d!@#$%^&*()_\\\\-+=]{9,13}$")
    private String newPassword;
}
