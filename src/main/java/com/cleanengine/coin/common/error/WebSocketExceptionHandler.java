package com.cleanengine.coin.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(DomainValidationException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public String handleDomainValidationException(DomainValidationException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler(Exception.class)
    public void handleException(Exception e, @Payload String payload) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("=======Websocket 요청 중 Handling 되지 않는 에러 발생=======");
        logMessage.append("\n발생 시간 : ").append(LocalDateTime.now());
        logMessage.append("\n요청 내용 : ").append(payload);
        logMessage.append("\n예외 타입 : ").append(e.getClass().getName());
        logMessage.append("\n메시지 : ").append(e.getMessage());
        log.warn("{}", logMessage, e);
    }
}
