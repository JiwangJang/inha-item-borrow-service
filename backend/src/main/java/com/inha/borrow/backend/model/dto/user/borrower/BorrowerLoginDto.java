package com.inha.borrow.backend.model.dto.user.borrower;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/**
 * i-class 로그인을 위한 dto
 *
 * @author 형민재
 */
public class BorrowerLoginDto {
    String id;
    String password;
}
