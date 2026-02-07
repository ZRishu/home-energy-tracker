package org.zr.device_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTime {
    @Pointcut("execution(* org.zr.device_service.controller.*.*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.nanoTime();
            long duration = end - start;
            long durationInMillis = TimeUnit.NANOSECONDS.toMillis(duration);
            String signature = joinPoint.getSignature().toShortString();
            log.info("Controller method {} executed in {} ms", signature, durationInMillis);
        }
    }
}
