package com.cleanengine.coin.common.error;

import com.cleanengine.coin.common.response.ErrorStatus;

public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException(String message) {
        super(message, ErrorStatus.UNAUTHORIZED_RESOURCE);
    }
}
