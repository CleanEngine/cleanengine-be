package com.cleanengine.coin.common.response;

import com.cleanengine.coin.common.validation.ValidationHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"errorCode", "errorMessage", "fieldErrorDetails"})
public record ErrorResponse (
        String errorCode,
        String errorMessage,
        @JsonIgnore HttpStatus httpStatus,
        List<FieldErrorDetail> fieldErrorDetails
) {
    public ErrorResponse {}
    private ErrorResponse(final ErrorStatus status, final List<FieldErrorDetail> fieldErrorDetails) {
        this(status.getErrorCode(), status.getDefaultErrorMessage(), status.getHttpStatus(), fieldErrorDetails);
    }

    private ErrorResponse(final ErrorStatus status) {
        this(status.getErrorCode(), status.getDefaultErrorMessage(), status.getHttpStatus(), new ArrayList<>());
    }

    public static ErrorResponse of(final ErrorStatus status) {
        return new ErrorResponse(status);
    }

    // TODO 원래는 ConstraintViolation을 그대로 변환해서 반환하면 안되고, 필드 명까지 숨겨야 한다.
    public static ErrorResponse of(final ErrorStatus status, Collection<ConstraintViolation<?>> constraintViolations){
        List<FieldErrorDetail> extractedDetails =
                constraintViolations.stream()
                        .map((constraintViolation ->
                                new FieldErrorDetail(
                                        ValidationHelper.extractFieldNameFromPropertyPathStr(constraintViolation.getPropertyPath().toString()),
                                        defaultStringIfNull(constraintViolation.getInvalidValue(), "Null"),
                                        defaultStringIfNull(constraintViolation.getMessage(),"No message")
                                )))
                        .collect(Collectors.toList());

        return new ErrorResponse(status, extractedDetails);
    }

    public static ErrorResponse of(final ErrorStatus status, final List<FieldError> errors) {
        List<FieldErrorDetail> extractedDetails =
                errors.stream()
                        .map((error)->
                                new FieldErrorDetail(
                                        defaultStringIfNull(error.getField(), "None"),
                                        defaultStringIfNull(error.getRejectedValue(), "Null"),
                                        defaultStringIfNull(error.getDefaultMessage(), "No message")))
                        .collect(Collectors.toList());

        return new ErrorResponse(status, extractedDetails);
    }

    private static String defaultStringIfNull(Object obj, String defaultStr){
        return obj == null? defaultStr : obj.toString();
    }

    public record FieldErrorDetail(
            String fieldName,
            String rejectedValue,
            String constraintMessage
    ){}
}