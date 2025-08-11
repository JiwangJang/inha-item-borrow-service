package com.inha.borrow.backend.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SMS인증코드 인증시 사용하는 DTO
 * 
 * @author 장지왕
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SMSCodeVerifyDto {
    private String id;
    private String code;
}
