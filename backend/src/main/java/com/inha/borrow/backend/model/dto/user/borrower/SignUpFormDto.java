package com.inha.borrow.backend.model.dto.user.borrower;

import com.inha.borrow.backend.model.entity.SignUpForm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpFormDto {
    @Pattern(regexp = "^[a-zA-Z0-9]{4,10}$", message = "아이디는 영어대소문자와 숫자로 4~10자여야 합니다.")
    private String id;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[A-Za-z\\d!@#$%^&*()_\\-+=]{9,13}$", message = "비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다.")
    private String password;
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식에 맞지 않습니다.")
    private String email;
    @NotBlank(message = "이름을 작성해주세요.")
    private String name;
    @NotBlank(message = "핸드폰 번호를 기입해주세요.")
    private String phonenumber;
    @NotBlank(message = "환불계좌번호를 기입해주세요.")
    private String accountNumber;

    public SignUpForm getSignUpForm(String identityPhoto, String studentCouncilFee) {
        return SignUpForm.builder()
                .id(id)
                .password(password)
                .email(email)
                .name(name)
                .phonenumber(phonenumber)
                .accountNumber(accountNumber)
                .studentCouncilFeePhoto(studentCouncilFee)
                .identityPhoto(identityPhoto)
                .build();
    }
}
