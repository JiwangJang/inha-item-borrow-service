package com.inha.borrow.backend.model.exception;

public class PasswordMismatchException extends RuntimeException{
    public PasswordMismatchException(String massage){
        super(massage);
    }
}
