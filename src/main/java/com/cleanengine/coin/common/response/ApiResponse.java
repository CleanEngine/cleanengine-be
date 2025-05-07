package com.cleanengine.coin.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonPropertyOrder({"isSuccess", "data", "error"})
public record ApiResponse<T> (
        @JsonIgnore HttpStatus httpStatus,
        Boolean isSuccess,
        T data,
        ErrorResponse error
){
    public ApiResponse {}
    public static <T>ApiResponse<T> success(T data, HttpStatus httpStatus) {
        return new ApiResponse<>(httpStatus, true, data, null);
    }
    public static <T>ApiResponse<T> fail(ErrorResponse errResponse) {
        return new ApiResponse<>(errResponse.httpStatus(), false, null, errResponse);
    }
    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(this);
    }
}
