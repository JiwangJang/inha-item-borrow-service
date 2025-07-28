package com.inha.borrow.backend.model.dto;

import lombok.Data;

@Data
public class SignUpForm {
    String id;
    String password;
    String email;
    String name;
    String phoneNumber;
    String identityPhoto;
    String studentCouncilFeePhoto;
    String accountNumber;
}
