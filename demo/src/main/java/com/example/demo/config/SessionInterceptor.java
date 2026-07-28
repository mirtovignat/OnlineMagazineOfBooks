package com.example.demo.config;

import com.example.demo.dto.user.UserForOwnerViewDTO;
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
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse httpServletResponse,
                             @NotNull Object handler) throws Exception {

        UserForOwnerViewDTO user = (UserForOwnerViewDTO) request.getSession()
                .getAttribute("userForOwnerViewDTO");

        if (user == null) {
            if (isAjax(request)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.getWriter().write("{\"message\": \"Авторизуйтесь!\"}");
                return false;
            }
            httpServletResponse.sendRedirect("/login");
            return false;
        }
        return true;
    }
}