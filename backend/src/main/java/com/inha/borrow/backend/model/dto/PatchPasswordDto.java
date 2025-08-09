package com.inha.borrow.backend.model.dto;

import lombok.Data;

@Data
public class PatchPasswordDto {
    private String originPassword;
    private String newPassword;
}
