package com.example.demo.util;

import com.example.demo.model.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class UserSuffixGenerator {

    public String generate(User user) {
        Long userId = user.getId();
        String raw = user.getUsername() + "_" + userId;
        return DigestUtils.md5DigestAsHex(raw.getBytes()).substring(0, 8);
    }
}