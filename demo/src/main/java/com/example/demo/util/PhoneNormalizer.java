package com.example.demo.util;

import com.example.demo.dto.user.ProfileSettingsDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhoneNormalizer {

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizePhone(ProfileSettingsDTO profileSettingsDTO) {
        if (profileSettingsDTO == null) {
            return null;
        }
        return normalizePhone(profileSettingsDTO.phone());
    }

}
