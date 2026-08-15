package com.example.demo.config;

import com.example.demo.web.interceptor.BadgeUpdater;
import com.example.demo.web.interceptor.SessionInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;
    private final BadgeUpdater badgeUpdater;

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(sessionInterceptor)
                .addPathPatterns(
                        "/cart/**",
                        "/orders/**",
                        "/favourites/**",
                        "/profile/**",
                        "/rated/**",
                        "/history/**",
                        "/library/**"
                )
                .excludePathPatterns(
                        "/rated/*/reviews",
                        "/rated/rating/*",
                        "/rated/count/*"
                );
        interceptorRegistry.addInterceptor(badgeUpdater)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/webjars/**", "/error", "/login", "/register");
    }
}