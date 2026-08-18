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
                                                        Long currentUserId) {
        User user = getUser(currentUserId);
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
        User user = getUser(userId);
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

    public void validateTopUp(TopUpFormDTO topUpFormDTO, User user) {
        if (topUpFormDTO.amount() == null || topUpFormDTO.amount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = current.add(topUpFormDTO.amount());
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw BusinessException.of(ErrorCode.BALANCE_LIMIT_EXCEED, MAX_BALANCE);
        }
    }

    @Transactional
    public void saveProfile(ProfileSettingsDTO normalizedDto, Long currentUserId) {
        User user = getUser(currentUserId);
        userMapper.updateUserFromDto(normalizedDto, user);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String encodedPassword, Long userId) {
        User user = getUser(userId);
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
        favouriteMovieRepository.deleteAllByUserId(userId);
        cartItemRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Transactional
    public void deletePhone(Long userId) {
        User user = getUser(userId);
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

    public String getUsername(Long userId){
        return getUser(userId).getUsername();
    }

    public User getUser(Long userId) {
        return userRepository.findByIdOrThrow(userId);
    }

    public BigDecimal getBalance(Long userId) {
        return getUser(userId).getBalance();
    }

    @Transactional(readOnly = true)
    public long getPurchasesCount(Long userId) {
        return userRepository.countPurchasesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getRatingsCount(Long userId) {
        return userRepository.countRatingsByUserId(userId);
    }

    public WalletForOwnerViewDTO getWalletForOwner(Long userId) {
        return userMapper.toWalletView(getUser(userId));
    }

    public UserForOwnerViewDTO getUserForOwner(Long userId) {
        return userMapper.toOwnerView(getUser(userId));
    }

    public ProfileSettingsDTO getProfileSettings(Long userId) {
        return userMapper.toSettingsForm(getUser(userId));
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