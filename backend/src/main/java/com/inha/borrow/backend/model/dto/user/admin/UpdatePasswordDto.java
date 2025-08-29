package com.inha.borrow.backend.model.dto.user.admin;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDto {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[A-Za-z\\d!@#$%^&*()_\\-+=]{9,13}$", message = "비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다.")
    private String newPassword;
    private String originPassword;
}
