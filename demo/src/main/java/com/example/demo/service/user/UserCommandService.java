package com.example.demo.service.user;

import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.entity.UserMapper;
import com.example.demo.model.entity.User;
import com.example.demo.repository.entity.CartItemRepository;
import com.example.demo.repository.entity.FavouriteMovieRepository;
import com.example.demo.repository.entity.UserRepository;
import com.example.demo.util.PhoneNormalizer;
import com.example.demo.util.UserSuffixGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final UserSuffixGenerator userSuffixGenerator;

    public ProfileSettingsDTO prepareAndValidateProfile(ProfileSettingsDTO profileSettingsDTO,
                                                        Long currentUserId) {
        User user = userQueryService.getUser(currentUserId);
        String normalizedPhone = PhoneNormalizer.normalizePhone(profileSettingsDTO);
        ProfileSettingsDTO normalizedDto = userMapper.toSettingsForm(
                User.builder()
                        .username(profileSettingsDTO.username())
                        .email(profileSettingsDTO.email())
                        .phone(normalizedPhone)
                        .build()
        );
        if (normalizedDto.equals(userMapper.toSettingsForm(user))) {
            throw BusinessException.of(ErrorCode.DATA_COINCIDENCE);
        }
        validateUniqueness(normalizedDto.username(), normalizedDto.email(), normalizedDto.phone(), user);
        return normalizedDto;
    }

    public String prepareAndValidatePassword(PasswordChangingDTO passwordChangingDTO,
                                             Long userId) {
        User user = userQueryService.getUser(userId);
        passwordChangingDTO.isMismatch();
        passwordChangingDTO.isCoincidence();
        if (!passwordEncoder.matches(passwordChangingDTO.currentPassword(),
                user.getPasswordHash())) {
            throw BusinessException.of(ErrorCode.PASSWORD_INVALID);
        }
        return encodePassword(passwordChangingDTO.rawPassword());
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void saveProfile(ProfileSettingsDTO normalizedDto, Long currentUserId) {
        User user = userQueryService.getUser(currentUserId);
        userMapper.updateUserFromDto(normalizedDto, user);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String encodedPassword, Long userId) {
        User user = userQueryService.getUser(userId);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public void saveTopUp(BigDecimal amount, User user) {
        user.addMoney(amount);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userQueryService.getUser(userId);
        favouriteMovieRepository.deleteAllByUserId(userId);
        cartItemRepository.deleteAllByUserId(userId);
        String suffix = userSuffixGenerator.generate(user);
        userRepository.softDeleteById(userId, suffix);
    }

    @Transactional
    public void deletePhone(Long userId) {
        User user = userQueryService.getUser(userId);
        ProfileSettingsDTO dtoWithNullPhone = ProfileSettingsDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        userMapper.updateUserFromDto(dtoWithNullPhone, user);
        userRepository.save(user);
    }

    @Transactional
    public void createUser(User user) {
        userRepository.save(user);
    }

    private void validateUniqueness(String username, String email, String phone, User user) {
        if (!Objects.equals(username, user.getUsername()) && userRepository.existsByUsername(username)) {
            throw BusinessException.of(ErrorCode.ALREADY_TAKEN);
        }
        if (!Objects.equals(email, user.getEmail()) && userRepository.existsByEmail(email)) {
            throw BusinessException.of(ErrorCode.ALREADY_TAKEN);
        }
        if (phone != null && !Objects.equals(phone, user.getPhone()) && userRepository.existsByPhone(phone)) {
            throw BusinessException.of(ErrorCode.ALREADY_TAKEN);
        }
    }
}