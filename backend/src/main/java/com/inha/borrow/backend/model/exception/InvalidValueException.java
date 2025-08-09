package com.inha.borrow.backend.model.exception;

import com.inha.borrow.backend.model.exception.abstractException.ServiceException;

/**
 * 사용자가 잘못된 값을 입력했을때 발생하는 에러
 * 에러코드와 메시지를 통해 잘못된 점을 알려준다
 * 
 * @author 장지왕
 */
public class InvalidValueException extends ServiceException {
    public InvalidValueException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
