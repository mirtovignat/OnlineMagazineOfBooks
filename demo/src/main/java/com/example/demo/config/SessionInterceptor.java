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
                             @NonNull Object handler)
            throws Exception {
        String uri = httpServletRequest.getRequestURI();
        if (uri.startsWith("/user/") && !uri.equals("/user/movies") && !uri.equals("/user/cart/count") && !uri.equals("/user/favourites/count")) {
            UserForOwnerViewDTO user = (UserForOwnerViewDTO) httpServletRequest
                    .getSession().getAttribute("userForOwnerViewDTO");
            if (user == null) {
                httpServletResponse.sendRedirect("/login");
                return false;
            }
        }
        return true;
    }
}