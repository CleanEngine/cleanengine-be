package com.cleanengine.coin.common.annotation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class StartNewTraceAspect {

    @Around("@annotation(com.cleanengine.coin.common.annotation.StartNewTrace)")
    public Object createNewTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        Tracer tracer = GlobalOpenTelemetry.getTracer("com.cleanengine.coin");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        StartNewTrace newTraceAnnotation = method.getAnnotation(StartNewTrace.class);

        String spanName = newTraceAnnotation.value().isEmpty() ?
                signature.getDeclaringType().getSimpleName() + "." + method.getName() :
                newTraceAnnotation.value();

        Span span = tracer.spanBuilder(spanName).setNoParent().startSpan();

        try (Scope scope = span.makeCurrent()) {
            return joinPoint.proceed();
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}