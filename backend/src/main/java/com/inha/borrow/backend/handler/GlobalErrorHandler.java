package com.inha.borrow.backend.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.exception.ExistIdException;
import com.inha.borrow.backend.model.exception.InvalidCodeException;
import com.inha.borrow.backend.model.exception.InvalidIdException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ApiResponse<String>> invalidCodeException(InvalidCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ApiErrorCode.INVALID_CODE.name()));
    }

    /**
     * 이미있는 아이디를 아이디로 쓰려했을때 발생하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(ExistIdException.class)
    public ResponseEntity<ApiResponse<String>> existIdExceptionHandler(ExistIdException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ApiErrorCode.EXIST_ID.name()));
    }

    /**
     * 아이디 조건에 맞지 않는 값을 아이디로 쓰려했을때 발생하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<ApiResponse<String>> invalidIdExceptionHandler(InvalidIdException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ApiErrorCode.NOT_ALLOWED_VALUE.name()));
    }

    /**
     * DB조건에 맞지않는 값을 넣었을때 발생하는 예외를 처리하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> dataIntegrityViolationExceptionHandler(
            DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ApiErrorCode.NOT_ALLOWED_VALUE.name()));
    }

    /**
     * 찾으려는 자원이 없을때 발생하는 예외를 처리하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> resourceNotFoundExceptionHandler(
            ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ApiErrorCode.NOT_FOUND.name()));
    }

    /**
     * DB작업하다 발생한 예외를 처리하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<String>> dataAccessExceptionHandler(DataAccessException e) {
        log.error("DB관련 작업중 에러발생 : {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ApiErrorCode.DB_ERROR.name()));
    }

    /**
     * 일반적인 예외를 처리하는 핸들러
     * 
     * @param e
     * @return
     * @author 장지왕
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> generalExceptionHandler(Exception e) {
        log.error("에러 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ApiErrorCode.SERVER_ERROR.name()));
    }
}
