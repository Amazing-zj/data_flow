package com.mcg.controller.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Controller;

@Controller
@Aspect
public class ControllerCellTip {

//    @Before(value = "within(com.mcg..*Controller)")
//    public void cellBefore(JoinPoint p){
//        System.out.println("cell before " +p.getThis().getClass());
//    }
//
//    @Around(value ="execution(* com.mcg.controller.*.*(..))")
//    public void cellAround(ProceedingJoinPoint point) throws Throwable{
//        cellAroundFirst(point);
//        point.proceed();
//        cellAroundAfter();
//    }

    private void cellAroundFirst(JoinPoint point){
        System.out.println("cell "+point.getThis().getClass());
    }

    private void cellAroundAfter(JoinPoint... point){

    }



}
