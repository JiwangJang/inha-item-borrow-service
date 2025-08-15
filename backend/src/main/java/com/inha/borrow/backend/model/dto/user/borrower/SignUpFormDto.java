package com.inha.borrow.backend.model.dto.user.borrower;

import com.inha.borrow.backend.model.entity.SignUpForm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpFormDto {
    @NotBlank
    private String id;
    @NotBlank
    private String password;
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String accountNumber;

    public SignUpForm getSignUpForm(String studentIdentification, String studentCouncilFee) {
        return new SignUpForm(id, password, email, name, phoneNumber, studentIdentification, studentCouncilFee,
                accountNumber);
    }
}
