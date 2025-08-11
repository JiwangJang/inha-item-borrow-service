package com.inha.borrow.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpForm {
    private String id;
    private String password;
    private String email;
    private String name;
    private String phoneNumber;
    private String identityPhoto;
    private String studentCouncilFeePhoto;
    private String accountNumber;

}
