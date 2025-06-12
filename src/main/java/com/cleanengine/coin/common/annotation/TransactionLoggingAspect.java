package com.cleanengine.coin.common.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TransactionLoggingAspect {

    private static final String TRANSACTION_ID_KEY = "txId";

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String txId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRANSACTION_ID_KEY, txId);

        try {
            return joinPoint.proceed();
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }
}