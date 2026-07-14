package com.drzig.taskmanager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        logger.warn("403 Access Denied: {} {} — reason: {} — session: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(),
                request.getSession(false) != null ? request.getSession().getId() : "none");
        response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
    }
}
