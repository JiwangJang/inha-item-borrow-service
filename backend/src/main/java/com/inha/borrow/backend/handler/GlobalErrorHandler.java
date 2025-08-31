package com.inha.borrow.backend.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.ServiceLog;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.response.ErrorResponse;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
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
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiResponse<ErrorResponse> invalidValueExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, InvalidValueException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] invalid value: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 존재하지 않는 경로에 대한 요청을 보낼때 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(NoResourceFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ApiResponse<ErrorResponse> noResourceFoundExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, NoResourceFoundException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] no resource found: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.NOT_FOUND.name(), "해당 경로는 존재하지 않습니다.");
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 유효성 검사 실패한 경우 발생하는 예외 처리하는 핸들러
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(HandlerMethodValidationException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiResponse<ErrorResponse> handlerMethodValidationException(HttpServletRequest request,
                        @AuthenticationPrincipal User user, HandlerMethodValidationException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] handler method validation failed: {}", serviceLog);
                String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMessage);
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 컨트롤러의 값 조건에 맞지 않는 경우(NotNull 또는 NotBlank 등)
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiResponse<ErrorResponse> methodArgumentNotValidExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, MethodArgumentNotValidException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] method argument not valid: {}", serviceLog);
                FieldError fieldError = e.getBindingResult().getFieldError();
                String errorMessage = "잘못된 값을 제출하셨습니다. 올바른 값을 제출해주세요.";
                if (fieldError != null) {
                        errorMessage = fieldError.getDefaultMessage();
                }
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMessage);
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 컨트롤러의 값 조건에 맞지 않는 경우(NotNull 또는 NotBlank 등)
         * 
         * @param e
         * @return
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiResponse<ErrorResponse> httpMessageNotReadableExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, HttpMessageNotReadableException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] method argument not valid: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), "바디 양식을 다시 확인해주세요.");
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 찾으려는 자원이 없을때 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiResponse<ErrorResponse> resourceNotFoundExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, ResourceNotFoundException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] resource not found: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 사용자가 입력한 파일의 크기가 너무 클경우 발생하는 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
        public ApiResponse<ErrorResponse> maxUploadSizeExceedSizeExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, MaxUploadSizeExceededException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] file too large: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.FILE_SIZE_TOO_LARGE.name(),
                                ApiErrorCode.FILE_SIZE_TOO_LARGE.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 권한이 부족할때 발생하는 예외 처리
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ApiResponse<ErrorResponse> accessDeniedExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, AccessDeniedException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] access denied: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.NOT_ALLOWED.name(),
                                e.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 동시성 충돌 등 비정상 상태(409) 처리
         * 
         * @author 장지왕
         */
        @ExceptionHandler(IllegalStateException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ApiResponse<ErrorResponse> illegalStateExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, IllegalStateException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("INFO")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.info("[INFO] conflict/illegal state: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse("CONFLICT",
                                e.getMessage() != null ? e.getMessage() : "요청이 현재 리소스 상태와 충돌합니다.");
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 
         * /**
         * DB작업하다 발생한 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(DataAccessException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ApiResponse<ErrorResponse> dataAccessExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, DataAccessException e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("ERROR")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.error("[ERROR] database error: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.DB_ERROR.name(),
                                ApiErrorCode.DB_ERROR.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }

        /**
         * 일반적인 예외를 처리하는 핸들러
         * 
         * @param e
         * @return
         * @author 장지왕
         */
        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ApiResponse<ErrorResponse> generalExceptionHandler(HttpServletRequest request,
                        @AuthenticationPrincipal User user, Exception e) {
                ServiceLog serviceLog = ServiceLog.builder()
                                .errorRank("ERROR")
                                .exceptionName(e.getClass().getName())
                                .cause(e)
                                .message(e.getMessage())
                                .userId(user == null ? "anonymous" : user.getId())
                                .authority(user == null ? "NOT_USER" : user.getAuthorities().get(0).getAuthority())
                                .requestPath(request.getRequestURI())
                                .requestMethod(request.getMethod())
                                .queryString(request.getQueryString())
                                .build();
                log.error("[ERROR] server error: {}", serviceLog);
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.SERVER_ERROR.name(),
                                ApiErrorCode.SERVER_ERROR.getMessage());
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false, errorResponse);
                return apiResponse;
        }
}
