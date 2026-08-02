package com.example.demo.service;

import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.Normalizer;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserService {
    private static final BigDecimal MAX_BALANCE = new BigDecimal("100000.00");
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;
    private final Normalizer normalizer;

    public User findUserByUsername(String username) {
        return userRepository.findByUsernameOrThrow(username);
    }

    private void throwIfNoChanges(boolean unchanged) {
        if (unchanged) {
            throw BusinessException.of(ErrorCode.DATA_COINCIDENCE);
        }
    }

    @Transactional
    public void changeProfile(ProfileSettingsDTO profileSettingsDTO, User user) {
        String normalizedPhone = normalizer.normalizePhone(profileSettingsDTO);
        ProfileSettingsDTO normalizedDto = new ProfileSettingsDTO(
                profileSettingsDTO.username(),
                profileSettingsDTO.email(),
                normalizedPhone
        );
        throwIfNoChanges(normalizedDto.equals(userMapper.toSettingsForm(user)));
        validateUniqueness(
                normalizedDto.username(),
                normalizedDto.email(),
                normalizedDto.phone(),
                user
        );
        userMapper.updateUserFromDto(normalizedDto, user);
        userRepository.save(user);
    }

    @Transactional
    public void changeProfile(ProfileSettingsDTO dto, String username) {
        User user = findUserByUsername(username);
        changeProfile(dto, user);
    }

    @Transactional
    public void changePassword(PasswordChangingDTO passwordChangingDTO, User user) {
        passwordChangingDTO.isMismatch();
        passwordChangingDTO.isCoincidence();

        if (!passwordEncoder.matches(passwordChangingDTO.currentPassword(), user.getPasswordHash())) {
            throw BusinessException.of(ErrorCode.PASSWORD_INVALID);
        }

        user.setPasswordHash(passwordEncoder.encode(passwordChangingDTO.rawPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(PasswordChangingDTO dto, String username) {
        User user = findUserByUsername(username);
        changePassword(dto, user);
    }

    @Transactional
    public void deleteAccount(String username) {
        favouriteMovieRepository.deleteAllByUsername(username);
        cartItemRepository.deleteAllByUsername(username);
        userRepository.softDeleteByUsername(username);
    }

    @Transactional
    public void topUp(TopUpFormDTO topUpFormDTO, User user) {
        if (topUpFormDTO.amount() == null || topUpFormDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = current.add(topUpFormDTO.amount());
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw BusinessException.of(ErrorCode.BALANCE_LIMIT_EXCEED, MAX_BALANCE);
        }

        user.addMoney(topUpFormDTO.amount());
        userRepository.save(user);
    }

    public WalletForOwnerViewDTO getWalletForOwner(String username) {
        return userMapper.toWalletView(userRepository.findByUsernameOrThrow(username));
    }

    public UserForOwnerViewDTO getUserForOwner(String username) {
        return userMapper.toOwnerView(findUserByUsername(username));
    }

    public ProfileSettingsDTO getProfileSettings(String username) {
        User user = findUserByUsername(username);
        return userMapper.toSettingsForm(user);
    }

    @Transactional
    public void deletePhone(String username) {
        User user = findUserByUsername(username);
        user.setPhone(null);
        userRepository.save(user);
    }

    private void validateUniqueness(String username, String email, String phone, User user) {
        if (!Objects.equals(username, user.getUsername()) && userRepository.existsByUsername(username)) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }

        if (!Objects.equals(email, user.getEmail()) && userRepository.existsByEmail(email)) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }

        if (phone != null && !Objects.equals(phone, user.getPhone()) && userRepository.existsByPhone(phone)) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }
    }
}