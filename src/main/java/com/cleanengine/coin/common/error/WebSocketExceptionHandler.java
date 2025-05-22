package com.cleanengine.coin.common.error;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(DomainValidationException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public String handleDomainValidationException(DomainValidationException e) {
        return e.getMessage();
    }
}
