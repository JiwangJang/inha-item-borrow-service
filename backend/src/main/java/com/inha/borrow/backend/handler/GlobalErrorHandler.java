package com.inha.borrow.backend.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.model.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

        /**
         * 사용자가 유효하지 않은 값을 넣었을때 발생하는 예외
         * 에러코드와 메시지로 잘못된 사항을 알려준다
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(InvalidValueException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> invalidValueExceptionHandler(InvalidValueException e) {
                log.error("[ERROR] 사용자의 잘못된 값으로 인한 에러 : {}", e);
                ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.badRequest().body(apiResponse);
        }

        /**
         * 찾으려는 자원이 없을때 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> resourceNotFoundExceptionHandler(
                        ResourceNotFoundException e) {
                log.error("[ERROR] 사용자가 찾으려는 값이 없음 : {}", e);
                ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(apiResponse);
        }

        /**
         * DB작업하다 발생한 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> dataAccessExceptionHandler(DataAccessException e) {
                log.error("[ERROR] 데이터베이스 에러 : {}", e);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.DB_ERROR.name(),
                                ApiErrorCode.DB_ERROR.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(apiResponse);
        }

        /**
         * 일반적인 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> generalExceptionHandler(Exception e) {
                log.error("[ERROR] 서버 에러 : {}", e);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.SERVER_ERROR.name(),
                                ApiErrorCode.SERVER_ERROR.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(apiResponse);
        }
}
