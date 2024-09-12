package org.example.expert.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.example.expert.config.UserDetailsImpl;
import java.io.IOException;

@Slf4j
public class MockTestFilter implements Filter {

    /**
     * Mock Test용 우회 필터
     * @param servletRequest Mock principal이 담긴 Request 객체
     * @param servletResponse Attribute가 담길 Responce객체
     * @param filterChain 다음 Filter Chain
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        try {


            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

}
