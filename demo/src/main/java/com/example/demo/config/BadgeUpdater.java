package com.example.demo.config;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.BadgeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@AllArgsConstructor
public class BadgeUpdater implements HandlerInterceptor {

    private final BadgeService badgeService;

    @Override
    public void postHandle(@NotNull HttpServletRequest httpServletRequest,
                           @NotNull HttpServletResponse httpServletResponse,
                           @NotNull Object handler,
                           ModelAndView modelAndView) {

        if (modelAndView == null || isRedirectView(modelAndView)) {
            return;
        }

        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            badgeService.getDefaultBadges().forEach(modelAndView::addObject);
            return;
        }

        Object attribute = session.getAttribute("userForOwnerViewDTO");
        if (!(attribute instanceof UserForOwnerViewDTO user)) {
            badgeService.getDefaultBadges().forEach(modelAndView::addObject);
            return;
        }

        badgeService.getBadgeCounts(user.username())
                .forEach(modelAndView::addObject);
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        String viewName = modelAndView.getViewName();
        return viewName != null && viewName.startsWith("redirect:");
    }
}