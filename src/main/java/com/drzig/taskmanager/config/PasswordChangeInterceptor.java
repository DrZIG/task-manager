package com.drzig.taskmanager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return true;
        if (!(auth.getPrincipal() instanceof CustomUserDetails user)) return true;

        String uri = request.getRequestURI();

        // Allow these through unconditionally
        if (uri.contains("/change-password") ||
                uri.contains("/logout") ||
                uri.contains("/css/") ||
                uri.contains("/js/")) {
            return true;
        }

        if (user.isMustChangePassword()) {
            response.sendRedirect(request.getContextPath() + "/change-password");
            return false;
        }

        return true;
    }
}
