package com.example.demo.web.interceptor;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.service.BadgeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
@AllArgsConstructor
public class BadgeUpdater implements HandlerInterceptor {

    private final BadgeService badgeService;

    @Override
    public void postHandle(@NotNull HttpServletRequest httpServletRequest,
                           @NotNull HttpServletResponse httpServletResponse,
                           @NotNull Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView == null || isRedirectView(modelAndView) || isAjax(httpServletRequest)) {
            return;
        }
        String uri = httpServletRequest.getRequestURI();
        Object errorAttr = httpServletRequest.getAttribute("jakarta.servlet.error.request_uri");
        if (uri.startsWith("/error") || errorAttr != null) {
            return;
        }
        HttpSession httpSession = httpServletRequest.getSession(false);
        BadgeCountsDTO badgeCountsDTO = BadgeCountsDTO.empty();
        if (httpSession != null) {
            Long userId = getUserIdFromSession(httpSession);
            if (userId != null) {
                try {
                    badgeCountsDTO = badgeService.getBadgeCounts(userId);
                } catch (Exception e) {
                    log.error("Failed to fetch badge counts for user: {}", userId, e);
                    badgeCountsDTO = BadgeCountsDTO.empty();
                }
            }
        }
        modelAndView.addObject("cartCount", badgeCountsDTO.cartCount());
        modelAndView.addObject("favouritesCount", badgeCountsDTO.favouritesCount());
        modelAndView.addObject("purchasesCount", badgeCountsDTO.purchasesCount());
        modelAndView.addObject("ratingsCount", badgeCountsDTO.ratingsCount());
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        String viewName = modelAndView.getViewName();
        return viewName != null && (viewName.startsWith("redirect:") || viewName.startsWith("forward:"));
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private Long getUserIdFromSession(HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("sessionUser");
        return sessionUser != null ? sessionUser.id() : null;
    }
}