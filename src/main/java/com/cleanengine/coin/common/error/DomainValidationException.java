package com.cleanengine.coin.common.error;

import com.cleanengine.coin.common.response.ErrorStatus;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
public class DomainValidationException extends BusinessException {
    private final List<FieldError> fieldErrors;

    public DomainValidationException(String message, List<FieldError> fieldErrors) {
        super(message, ErrorStatus.INVALID_INPUT_VALUE);
        this.fieldErrors = fieldErrors;
    }
}
