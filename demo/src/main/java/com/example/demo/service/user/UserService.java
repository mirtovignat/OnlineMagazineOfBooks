package com.example.demo.service.user;

import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.normalizer.PhoneNormalizer;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.FavouriteMovieRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final BigDecimal MAX_BALANCE = new BigDecimal("100000.00");
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CartItemRepository cartItemRepository;
    private final FavouriteMovieRepository favouriteMovieRepository;

    public ProfileSettingsDTO prepareAndValidateProfile(ProfileSettingsDTO profileSettingsDTO,
                                                        String currentUsername) {
        User user = getUser(currentUsername);
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
                                             String username) {
        User user = getUser(username);
        passwordChangingDTO.isMismatch();
        passwordChangingDTO.isCoincidence();
        if (!passwordEncoder.matches(passwordChangingDTO.currentPassword(), user.getPasswordHash())) {
            throw BusinessException.of(ErrorCode.PASSWORD_INVALID);
        }
        return encodePassword(passwordChangingDTO.rawPassword());
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void validateTopUp(TopUpFormDTO topUpFormDTO, User user) {
        if (topUpFormDTO.amount() == null || topUpFormDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = current.add(topUpFormDTO.amount());
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw BusinessException.of(ErrorCode.BALANCE_LIMIT_EXCEED, MAX_BALANCE);
        }
    }

    @Transactional
    public void saveProfile(ProfileSettingsDTO normalizedDto, String currentUsername) {
        User user = getUser(currentUsername);
        userMapper.updateUserFromDto(normalizedDto, user);
        userRepository.save(user);
    }

    @Transactional
    public void savePassword(String encodedPassword, String username) {
        User user = getUser(username);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public void saveTopUp(BigDecimal amount, User user) {
        user.addMoney(amount);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String username) {
        favouriteMovieRepository.deleteAllByUsername(username);
        cartItemRepository.deleteAllByUsername(username);
        userRepository.softDeleteByUsername(username);
    }

    @Transactional
    public void deletePhone(String username) {
        User user = getUser(username);
        ProfileSettingsDTO dtoWithNullPhone = new ProfileSettingsDTO(
                user.getUsername(),
                user.getEmail(),
                null
        );
        userMapper.updateUserFromDto(dtoWithNullPhone, user);
        userRepository.save(user);
    }

    @Transactional
    public void createUser(User user) {
        userRepository.save(user);
    }

    public User getUser(String username) {
        return userRepository.findByUsernameOrThrow(username);
    }

    public String getUsername(UserForOwnerViewDTO userForOwnerViewDTO) {
        return Optional.ofNullable(userForOwnerViewDTO)
                .map(UserForOwnerViewDTO::username)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public long getPurchasesCount(String username) {
        return userRepository.countPurchasesByUsername(username);
    }

    @Transactional(readOnly = true)
    public long getRatingsCount(String username) {
        return userRepository.countRatingsByUsername(username);
    }

    public WalletForOwnerViewDTO getWalletForOwner(String username) {
        return userMapper.toWalletView(getUser(username));
    }

    public UserForOwnerViewDTO getUserForOwner(String username) {
        return userMapper.toOwnerView(getUser(username));
    }

    public ProfileSettingsDTO getProfileSettings(String username) {
        return userMapper.toSettingsForm(getUser(username));
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