package com.inha.borrow.backend.model.exception.abstractException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ServiceException extends RuntimeException {
    String errorCode;
    String errorMessage;

    public ServiceException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
