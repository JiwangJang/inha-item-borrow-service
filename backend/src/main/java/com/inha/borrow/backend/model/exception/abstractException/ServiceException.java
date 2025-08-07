package com.inha.borrow.backend.model.exception.abstractException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public abstract class ServiceException extends RuntimeException {
    String errorCode;
    String errorMessage;
}
