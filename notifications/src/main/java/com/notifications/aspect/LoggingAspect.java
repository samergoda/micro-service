package com.notifications.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Centralizes cross-cutting logging so that the consumer and service classes
 * stay focused on business logic. Logs method entry, successful completion
 * (with timing) and exceptions for the Kafka consumer and the service layer.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /** Any method on the Kafka consumer. */
    @Pointcut("execution(* com.notifications.consumer..*(..))")
    public void consumerLayer() {
    }

    /** Any public method on the service layer. */
    @Pointcut("execution(public * com.notifications.service.NotificationService.*(..))")
    public void serviceLayer() {
    }

    @Around("consumerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (log.isDebugEnabled()) {
            log.debug("-> {}({})", method, Arrays.toString(args));
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("{} completed in {} ms", method, took);
            return result;
        } catch (Throwable ex) {
            log.error("{} failed: {}", method, ex.getMessage(), ex);
            throw ex;
        }
    }
}
