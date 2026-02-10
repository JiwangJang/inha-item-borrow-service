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
 * 기존 사용자 정보를 저장하는 캐쉬 dto
 *
 * @author 형민재
 */
public class CacheBorrowerDto {
    String id;
    String name;
    String department;
    String phoneNumber;
    String accountNumber;
    boolean ban;
    boolean verify;
    String s3Link;
    String agreementVersion;
}
