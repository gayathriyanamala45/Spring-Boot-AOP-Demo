package com.example.aopdemo;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.example.aopdemo.MyService.display(..))")
    public void logBefore() {
        System.out.println("AOP: Before method execution");
    }
}