package com.inha.borrow.backend.model.dto.auth;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordVerifyRequestDto {
    private String id;
    // 조건 : 영어 대소문자와 숫자, 특수기호(!@#$%^&*()_\-+=)를 포함하여 9~13자
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[A-Za-z\\d!@#$%^&*()_\\-+=]{9,13}$", message = "비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다.")
    private String password;
}
