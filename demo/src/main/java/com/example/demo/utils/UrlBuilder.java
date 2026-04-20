package com.example.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {

    public String buildMoviesUrl(String fullName) {
        if (fullName == null || fullName.isBlank()) return "#";
        return "/movie?director=" +
                java.net.URLEncoder.encode(fullName, java.nio.charset.StandardCharsets.UTF_8);
    }
}
