package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                .addPathPatterns(
                        "/profile/**",
                        "/cart/**",
                        "/favourites/**",
                        "/wallet/**",
                        "/orders/**",
                        "/rated/add",
                        "/rated/edit",
                        "/rated/remove/**"
                )
                .excludePathPatterns(
                        "/cart/count",
                        "/favourites/count"
                );
    }
}