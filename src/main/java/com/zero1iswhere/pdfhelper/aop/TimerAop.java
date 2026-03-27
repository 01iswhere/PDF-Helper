package com.zero1iswhere.pdfhelper.aop;

import com.zero1iswhere.pdfhelper.annotation.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Slf4j
@Component
public class TimerAop {

    @Around("@annotation(timer)")
    public Object recordExecutionTime(ProceedingJoinPoint joinPoint, Timer timer) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        log.info("{}操作耗时:" + stopWatch.getTotalTimeMillis(), timer.name());
        return result;
    }
}
