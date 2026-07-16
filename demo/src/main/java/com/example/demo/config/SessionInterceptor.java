package com.example.demo.config;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             @NonNull HttpServletResponse httpServletResponse,
                             @NonNull Object handler) throws Exception {

        UserForOwnerViewDTO userForOwnerViewDTO = (UserForOwnerViewDTO) httpServletRequest
                .getSession().getAttribute("userForOwnerViewDTO");

        if (userForOwnerViewDTO == null) {
            httpServletResponse.sendRedirect("/login");
            return false;
        }

        return true;
    }
}