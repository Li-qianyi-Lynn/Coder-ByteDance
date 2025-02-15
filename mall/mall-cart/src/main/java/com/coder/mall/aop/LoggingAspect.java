//package com.coder.mall.aop;
//
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class LoggingAspect {
//    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
//
//    @Before("execution(* com.example.service..*(..))")
//    public void logBefore() {
//        logger.info("Method execution start");
//    }
//
//    @AfterReturning(pointcut = "execution(* com.example.service..*(..))", returning = "result")
//    public void logAfterReturning(Object result) {
//        logger.info("Method execution end, result: {}", result);
//    }
//
//    @AfterThrowing(pointcut = "execution(* com.example.service..*(..))", throwing = "exception")
//    public void logAfterThrowing(Throwable exception) {
//        logger.error("Method execution threw an exception", exception);
//    }
//}
