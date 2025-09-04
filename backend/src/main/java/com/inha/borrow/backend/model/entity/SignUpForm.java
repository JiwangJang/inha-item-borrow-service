package com.inha.borrow.backend.model.entity;

import java.sql.Timestamp;

import com.inha.borrow.backend.enums.SignUpRequestState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpForm {
    private String id;
    private String password;
    private String email;
    private String name;
    private String phonenumber;
    private String identityPhoto;
    private String studentCouncilFeePhoto;
    private String accountNumber;
    private Timestamp created_at;
    private SignUpRequestState state;
    private String rejectReason;
}
