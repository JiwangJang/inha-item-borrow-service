package com.inha.borrow.backend.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SMS인증코드 요청시 사용하는 Dto
 * 
 * @author 장지왕
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SMSCodeRequestDto {
    String id;
    String phoneNumber;
}
