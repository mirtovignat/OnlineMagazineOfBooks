package com.example.demo.web.interceptor;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    private boolean isAjax(HttpServletRequest httpServletRequest) {
        String requestedWith = httpServletRequest.getHeader("X-Requested-With");
        String accept = httpServletRequest.getHeader("Accept");
        String contentType = httpServletRequest.getHeader("Content-Type");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"))
                || (contentType != null && contentType.contains("application/json"));
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpServletRequest,
                             @NotNull HttpServletResponse httpServletResponse,
                             @NotNull Object handler) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        if (uri.equals("/") || uri.startsWith("/login") || uri.startsWith("/register")
                || uri.startsWith("/error") || uri.startsWith("/rated/") || uri.startsWith("/movies/")) {
            return true;
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        UserForOwnerViewDTO userForOwnerViewDTO = (httpSession != null)
                ? (UserForOwnerViewDTO) httpSession.getAttribute("userForOwnerViewDTO")
                : null;
        if (userForOwnerViewDTO == null) {
            if (isAjax(httpServletRequest)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.getWriter().write("{\"message\": \"" +
                        ErrorCode.NOT_AUTHORIZED.format() + "\"}");
                return false;
            }
            httpServletResponse.sendRedirect("/login");
            return false;
        }
        return true;
    }
}