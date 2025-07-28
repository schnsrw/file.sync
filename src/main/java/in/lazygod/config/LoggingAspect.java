package in.lazygod.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("API START {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        try {
            return joinPoint.proceed();
        } finally {
            long time = System.currentTimeMillis() - start;
            log.info("API END {}.{} took {} ms", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), time);
        }
    }
}
