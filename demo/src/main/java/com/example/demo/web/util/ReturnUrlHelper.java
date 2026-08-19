package com.example.demo.web.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class ReturnUrlHelper {

    private static final String RETURN_URL_ATTR = "returnUrl";

    public void saveReturnUrl(HttpSession session, String url) {
        if (url != null && !url.isBlank()) {
            session.setAttribute(RETURN_URL_ATTR, url);
        }
    }

    public String getAndClearReturnUrl(HttpSession session) {
        String url = (String) session.getAttribute(RETURN_URL_ATTR);
        session.removeAttribute(RETURN_URL_ATTR);
        return url;
    }

    public String getReturnUrlOrDefault(HttpSession session, String defaultUrl) {
        String url = getAndClearReturnUrl(session);
        return url != null ? url : defaultUrl;
    }

    public String getReturnUrlOrDefault(HttpSession session) {
        return getReturnUrlOrDefault(session, "/");
    }

    public void clearReturnUrl(HttpSession session) {
        session.removeAttribute(RETURN_URL_ATTR);
    }

    public boolean hasReturnUrl(HttpSession session) {
        String url = (String) session.getAttribute(RETURN_URL_ATTR);
        return url != null && !url.isBlank();
    }
}