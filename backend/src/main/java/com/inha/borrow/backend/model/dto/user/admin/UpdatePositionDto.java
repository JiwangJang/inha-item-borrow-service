package com.inha.borrow.backend.model.dto.user.admin;

import com.inha.borrow.backend.enums.Role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePositionDto {
    @NotNull
    private Role position;
}
