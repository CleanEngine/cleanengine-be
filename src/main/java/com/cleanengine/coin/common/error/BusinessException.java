package com.cleanengine.coin.common.error;

import com.cleanengine.coin.common.response.ErrorStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public BusinessException(String message, ErrorStatus errorStatus) {
        super(message);
        this.errorStatus = errorStatus;
    }
}
