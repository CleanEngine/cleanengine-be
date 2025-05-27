package com.cleanengine.coin.common.error;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.common.response.ErrorResponse;
import com.cleanengine.coin.common.response.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.INVALID_INPUT_VALUE, e.getBindingResult().getFieldErrors());
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e){
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.INVALID_INPUT_VALUE, e.getConstraintViolations());
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.API_ENDPOINT_NOT_EXIST);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorStatus errorStatus = ErrorStatus.INVALID_TYPE;
        String mismatchedValue = e.getValue() != null ? e.getValue().toString() : "";
        List<FieldError> errors = List.of(new FieldError(e.getName(), mismatchedValue, e.getErrorCode()));

        ErrorResponse response = ErrorResponse.of(errorStatus, errors);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.METHOD_NOT_SUPPORTED);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.INVALID_INPUT_FORMAT);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ApiResponse<Object>> handleNoStaticResourceFoundException(NoResourceFoundException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.API_ENDPOINT_NOT_EXIST);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.INVALID_INPUT_VALUE);
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(DomainValidationException.class)
    protected ResponseEntity<ApiResponse<Object>> handleDomainValidationException(DomainValidationException e) {
        final ErrorResponse response = ErrorResponse.of(e.getErrorStatus(), e.getFieldErrors());
        return ApiResponse.fail(response).toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
        final ErrorResponse response = ErrorResponse.of(ErrorStatus.INTERNAL_SERVER_ERROR);
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("=======API 요청 중 Handling 되지 않는 에러 발생=======");
        logMessage.append("\n발생 시간 : ").append(LocalDateTime.now());
        logMessage.append("\n요청 URI : ").append(request.getRequestURI());
        logMessage.append("\n예외 타입 : ").append(e.getClass().getName());
        logMessage.append("\n메시지 : ").append(e.getMessage());
        log.warn("{}", logMessage, e);

        return ApiResponse.fail(response).toResponseEntity();
    }
}
