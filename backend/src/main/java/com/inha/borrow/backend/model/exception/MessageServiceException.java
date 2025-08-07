package com.inha.borrow.backend.model.exception;

import com.inha.borrow.backend.model.exception.abstractException.ServiceException;

/**
 * 메세징 서비스에서 에러가 발생했음을 알리는 예외
 * 
 * @author 장지왕
 */
public class MessageServiceException extends ServiceException {
    public MessageServiceException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
