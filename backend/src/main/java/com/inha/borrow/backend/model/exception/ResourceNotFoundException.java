package com.inha.borrow.backend.model.exception;

import com.inha.borrow.backend.model.exception.abstractException.ServiceException;

/**
 * 찾으려는 자원이 없음을 알리는 예외
 * 
 * @author 장지왕
 */
public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
