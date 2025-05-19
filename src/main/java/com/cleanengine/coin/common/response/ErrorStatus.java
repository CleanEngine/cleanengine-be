package com.cleanengine.coin.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorStatus {

    // Common
    METHOD_NOT_SUPPORTED("A01", HttpStatus.METHOD_NOT_ALLOWED, "Method is not allowed. Check HTTP Method."),
    INVALID_TYPE("A02", HttpStatus.BAD_REQUEST, "Invalid Type entered"),
    INVALID_INPUT_VALUE("A03", HttpStatus.BAD_REQUEST, "Invalid Input entered"),
    API_ENDPOINT_NOT_EXIST("A04", HttpStatus.NOT_FOUND, "API Endpoint doesn't exist"),
    UNAUTHORIZED_RESOURCE("A05", HttpStatus.BAD_REQUEST, "Current user can't access this resource"),
    INTERNAL_SERVER_ERROR("A88", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Server Error occurred. Contact Backend Admin."),
    NOT_CLASSIFIED_BUSINESS_ERROR("A99", HttpStatus.BAD_REQUEST, "Unclassified Business Error.");


    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String defaultErrorMessage;

    ErrorStatus(final String errorCode, final HttpStatus httpStatus, final String defaultErrorMessage) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.defaultErrorMessage = defaultErrorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultErrorMessage() {
        return this.defaultErrorMessage;
    }

}
