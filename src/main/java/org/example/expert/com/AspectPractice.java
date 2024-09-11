package org.example.expert.com;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Slf4j
@Aspect
public class AspectPractice {
    // 포인트컷들

    /**
     * 포인트컷: 서비스 패키지 기반
     */
    @Pointcut("execution(* org.example.expert..*(..))")
    private void serviceLayer(){}



    /**
     * 포인트컷: 어노테이션 범위 기반
     */
    @Pointcut("@annotation(org.example.expert.annotation.TrackTime)")
    private void trackTimeAnnotation(){}

    /*
    * 어드바이스: 어노테이션 범위 기반
    * */
    @Around("trackTimeAnnotation()")
    public Object adviceAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 즉정 시작
        long startTime =System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // 측정 완료
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("::: Execution Time: {} ms", executionTime);
        }
    }


//    public Object advicePackageMethod(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//        try{
//            Object result = joinPoint.proceed();
//            return result;
//        }finally {
//            long endTime = System.currentTimeMillis();
//            long executionTime = endTime - startTime;
//            log.info("::: Execution Time: {} ms", executionTime);
//
//        }
//    }

    // 어드바이스들
    /**
     * 어드바이스들 @Before
     * 메서드 실행 전에 수행되는 로직을 처리할때 사용
     */
    public void beforeMethod(){
        log.info("::: BEFORE 실행 :::");
    }

    /*
    * 어드바이스: @AfterReturning
    * 메서드가 정상적으로 반환된 후에 실행
    * 예외가  발생하지 않고 정상적으로 결과값이 반환됐을때 동작
    * */
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void afterReturningMethod(Object result){
        // result 관련 로직
        log.info("::: AFTER RETURNING :::");
    }

    /*
    * 어드바이스 : @AfterThrowing
    * 메서드 실행 중 예외가 발생했을때만 실행
    * */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void afterThrowingMethod(Throwable ex){
        // ex <- 예외가 발생했을때 필요한 로직
        log.info("::: AFTER THROWING :::");
    }

    /*
    * 어드바이스: @After
    * 메서드가 정상적으로 실행되건, 예외가 발생하건, 항상 실행
    * */
    @After("serviceLayer()")
    public void afterMethod(){
        log.info("::: AFTER METHOD :::");
    }

    /*
    * 어드바이스 : @Around
    * 가장 강력한 어드바이스, 전체 흐름을 제어할 수 있는 어드바이스
    * */
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info(":: BEFORE :::");
        try {
            Object result = joinPoint.proceed();
            log.info("::: AFTER RETURNING :::");
            return result;
        } catch (Exception e){
            log.info("::: AFTER THROWING :::");
            throw e;
        } finally {
            log.info("::: AFTER METHOD :::");
        }
    }
}
