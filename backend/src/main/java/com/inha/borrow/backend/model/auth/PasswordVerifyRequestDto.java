package com.inha.borrow.backend.model.auth;

import lombok.Data;

@Data
public class PasswordVerifyRequestDto {
    String id;
    String password;
}
