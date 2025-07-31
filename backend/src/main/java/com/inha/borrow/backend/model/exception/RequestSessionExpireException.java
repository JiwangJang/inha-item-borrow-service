package com.inha.borrow.backend.model.exception;

import lombok.NoArgsConstructor;

/**
 * 대여자의 인증상태를 저장하는 객체가 만료됐음을 알리는 예외
 * 
 * @author 장지왕
 */
@NoArgsConstructor
public class RequestSessionExpireException extends RuntimeException {

}
