package com.inha.borrow.backend.model.dto.user.borrower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowerDto {
    String id;
    String password;
    String email;
    String name;
    String phonenumber;
    String studentNumber;
    String accountNumber;
    String refreshToken;
}
