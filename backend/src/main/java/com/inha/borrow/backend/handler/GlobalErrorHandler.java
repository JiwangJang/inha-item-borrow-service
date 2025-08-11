package com.inha.borrow.backend.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.response.ErrorResponse;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

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
                log.error("[ERROR] 사용자의 잘못된 값으로 인한 에러 : ", e);
                ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.badRequest().body(apiResponse);
        }

        /**
         * 존재하지 않는 경로에 대한 요청을 보낼때 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> noResourceFoundExceptionHandler(NoResourceFoundException e) {
                log.error("[ERROR] 없는 경로에 대한 요청 : ", e);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.NOT_FOUND.name(), "해당 경로는 존재하지 않습니다.");
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }

        /**
         * 유효성 검사 실패한 경우 발생하는 예외 처리하는 핸들러
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> handlerMethodValidationException(
                        HandlerMethodValidationException e) {
                log.error("[ERROR] 유효성검사 실패 : ", e);
                String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMessage);
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.badRequest().body(apiResponse);
        }

        /**
         * 컨트롤러의 값 조건에 맞지 않는 경우(NotNull 또는 NotBlank 등)
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> methodArgumentNotValidExceptionHandler(
                        MethodArgumentNotValidException e) {
                log.error("[ERROR] 사용자의 잘못된 값으로 인한 에러 : ", e);
                FieldError fieldError = e.getBindingResult().getFieldError();
                String errorMessage = "잘못된 값을 제출하셨습니다. 올바른 값을 제출해주세요.";
                if (fieldError != null) {
                        errorMessage = fieldError.getDefaultMessage();
                }
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMessage);
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
         * 사용자가 입력한 파일의 크기가 너무 클경우 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiResponse<ErrorResponse>> maxUploadSizeExceedSizeExceptionHandler(
                        MaxUploadSizeExceededException e) {
                log.error("[ERROR] 5MB이상의 파일 : {}", e);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.FILE_SIZE_TOO_LARGE.name(),
                                ApiErrorCode.FILE_SIZE_TOO_LARGE.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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
