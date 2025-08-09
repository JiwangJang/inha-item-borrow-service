package com.inha.borrow.backend.handler;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.exception.PasswordMismatchException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> dataIntegrityViolationExceptionHandler(
            DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ApiErrorCode.NOT_ALLOWED_VALUE.name()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> resourceNotFoundExceptionHandler(
            ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ApiErrorCode.NOT_FOUND.name()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<String>> dataAccessExceptionHandler(DataAccessException e) {
        log.error("DB관련 작업중 에러발생 : {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ApiErrorCode.DB_ERROR.name()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> generalExceptionHandler(Exception e) {
        log.error("에러 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ApiErrorCode.SERVER_ERROR.name()));
    }
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<String>> passwordMismatchHandler(PasswordMismatchException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false,ApiErrorCode.PASSWORD_MISMATCH.name()));
    }

    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<ApiResponse<String>>failedUpload(AmazonS3Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false,ApiErrorCode.FAILED_UPLOAD.name()));
    }
}
