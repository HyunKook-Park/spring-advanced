package org.example.expert.com;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // CommentAdminController의 deleteComment 메서드 호출 전 로그 기록

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    public void deleteCommentPointcut(){}

    @Around("deleteCommentPointcut()")
    public Object logBeforeDeleteComment(ProceedingJoinPoint joinPoint ) throws Throwable {
        long currentTime = System.currentTimeMillis();
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = (Long) servletRequest.getAttribute("userId");
        log.info("::: 현재 시각 {} ", currentTime);
        log.info("::: url {} ", servletRequest.getRequestURI());
        log.info("::: userId {} ", userId);

        try {
            log.info("::: AFTER RETURNING :::");
            return joinPoint.proceed();
        } catch (Exception e) {
            log.info("::: AFTER THROWING :::");
            throw e;
        } finally {
            log.info("::: AFTER METHOD :::");
            long duration = System.currentTimeMillis() - currentTime;
            log.info(":::Execution Time : {} ms", duration);
        }
    }


    // UserAdminController
    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void changeUserRole() {}

    @Around("changeUserRole()")
    public Object logUserRole(ProceedingJoinPoint joinPoint) throws Throwable {
        long currentTime = System.currentTimeMillis();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = (Long) servletRequest.getAttribute("userId");
        String email = (String) servletRequest.getAttribute("email");
        UserRole userRole = UserRole.of((String) servletRequest.getAttribute("userRole"));

        log.info("::: 현재 시각 {} ", currentTime);
        log.info("::: url {} ", servletRequest.getRequestURI());
        log.info("::: userId {} ", userId, "::: email {} ", email, "::: userRole {} ", userRole);

        try {
            log.info("::: AFTER RETURNING :::");
            return joinPoint.proceed();
        } catch (Exception e){
            log.info("::: AFTER THROWING :::");
            throw e;
        }
        finally {
            log.info("::: AFTER METHOD :::");
            long duration = System.currentTimeMillis() - currentTime;
            log.info(":::Execution Time : {} ms", duration);
        }
    }
}
