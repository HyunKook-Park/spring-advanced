package org.example.expert.com;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class) ;
    // CommentAdminController의 deleteComment 메서드 호출 전 로그 기록

//    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
//    public void deleteCommentPointcut(){}
//
//    @Before("deleteCommentPointcut()")
//    public void logBeforeDeleteComment() {
//        logger.info("deleteComment API called");
//    }


    // UserAdminController
    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void changeUserRole() {}

    @Around("changeUserRole()")
    public void logUserRole(JoinPoint joinPoint) {
        // API 요청 시각
        String requestTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // API 요청 URL
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String apiUrl = request.getRequestURI();

        // 사용자 id 추출
        Object[] args = joinPoint.getArgs();
        long userId = 0;
        for(Object arg : args) {
            if(arg instanceof Long) {
                userId = (Long)arg;
                break;
            }
        }

        // 로그 기록
        logger.info("API Request - UserId: {}. Request Time: {}, API URL: {}", userId, requestTime, apiUrl);
    }

}
