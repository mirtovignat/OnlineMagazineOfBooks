package com.example.demo.config;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {

        UserForOwnerViewDTO user = (UserForOwnerViewDTO) request.getSession()
                .getAttribute("userForOwnerViewDTO");

        if (user == null) {
            if (isAjax(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"" + ErrorCode.NOT_AUTHORIZED.format() + "\"}");
                return false;
            }
            // Для обычных страниц перенаправляем на login
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}