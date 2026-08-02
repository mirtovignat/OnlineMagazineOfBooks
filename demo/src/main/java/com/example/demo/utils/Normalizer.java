package com.example.demo.utils;

import com.example.demo.dto.user.ProfileSettingsDTO;
import org.springframework.stereotype.Component;

@Component

public class Normalizer {

    public String normalizePhone(ProfileSettingsDTO profileSettingsDTO) {
        String normalizedPhone = profileSettingsDTO.phone() != null
                ? profileSettingsDTO.phone().trim()
                : null;
        if (normalizedPhone != null && normalizedPhone.isEmpty()) {
            normalizedPhone = null;
        }
        return normalizedPhone;
    }

}
