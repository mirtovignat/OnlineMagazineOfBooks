package com.example.demo.service;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.user.*;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.exception.purchased.BalanceLimitExceededException;
import com.example.demo.exception.user.DataCoincidenceException;
import com.example.demo.exception.user.InvalidPasswordException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityConfig securityConfig;

    public User findUserByUsername(String username) {
        return userRepository.findByUsernameOrThrow(username);
    }

    public boolean isProfileUnchanged(ProfileSettingsDTO profileSettingsDTO, User user) {
        return profileSettingsDTO.equals(userMapper.toSettingsForm(user));
    }

    public boolean isProfileUnchanged(UsernameChangingDTO usernameChangingDTO, User user) {
        return usernameChangingDTO.equals(userMapper.toUsernameChangingForm(user));
    }

    public boolean isProfileUnchanged(PhoneChangingDTO phoneChangingDTO, User user) {
        return phoneChangingDTO.equals(userMapper.toPhoneChangingForm(user));
    }

    public boolean isProfileUnchanged(EmailChangingDTO emailChangingDTO, User user) {
        return emailChangingDTO.equals(userMapper.toEmailChangingForm(user));
    }

    public boolean isProfileUnchanged(PasswordChangingDTO passwordChangingDTO, User user) {
        return passwordChangingDTO.equals(userMapper.toPasswordChangingForm(user));
    }

    private void throwIfNoChanges(boolean unchanged) {
        if (unchanged) {
            throw new DataCoincidenceException();
        }
    }

    @Transactional
    public void changeProfile(ProfileSettingsDTO profileSettingsDTO, User user) {
        throwIfNoChanges(isProfileUnchanged(profileSettingsDTO, user));
        userMapper.updateUserFromDto(profileSettingsDTO, user);
        userRepository.save(user);
    }

    @Transactional
    public void changeUsername(UsernameChangingDTO usernameChangingDTO, User user) {
        throwIfNoChanges(isProfileUnchanged(usernameChangingDTO, user));
        userMapper.updateUserFromUsernameChangingDTO(usernameChangingDTO, user);
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(EmailChangingDTO emailChangingDTO, User user) {
        throwIfNoChanges(isProfileUnchanged(emailChangingDTO, user));
        userMapper.updateUserFromEmailChangingDTO(emailChangingDTO, user);
        userRepository.save(user);
    }

    @Transactional
    public void changePhone(PhoneChangingDTO phoneChangingDTO, User user) {
        throwIfNoChanges(isProfileUnchanged(phoneChangingDTO, user));
        userMapper.updateUserFromPhoneChangingDTO(phoneChangingDTO, user);
        userRepository.save(user);
    }

    private void validatePasswordChanging(PasswordChangingDTO dto, User user) {
        dto.isMismatch();
        dto.isCoincidence();
        if (!securityConfig.passwordEncoder()
                .matches(dto.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }
    }

    @Transactional
    public void changePassword(PasswordChangingDTO passwordChangingDTO, User user) {
        validatePasswordChanging(passwordChangingDTO, user);
        throwIfNoChanges(isProfileUnchanged(passwordChangingDTO, user));
        userMapper.updateUserFromPasswordChangingDTO(passwordChangingDTO, user);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String username) {
        userRepository.deleteByUsername(username);
    }

    @Transactional
    public void topUp(TopUpFormDTO topUpFormDTO, User user) {
        if (topUpFormDTO.amount() == null || topUpFormDTO.amount() <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        double currentBalance = user.getBalance().doubleValue();
        double addAmount = topUpFormDTO.amount();
        double newBalance = currentBalance + addAmount;
        double MAX_BALANCE = 100_000.0;

        if (newBalance > MAX_BALANCE) {
            throw new BalanceLimitExceededException(currentBalance, addAmount);
        }

        BigDecimal addAmountBD = BigDecimal.valueOf(addAmount);
        user.addMoney(addAmountBD);
        userRepository.save(user);
    }

    public WalletForOwnerViewDTO getWalletForOwner(String username) {
        return userMapper.toWalletView(userRepository.findByUsernameOrThrow(username));
    }

    public UserForOwnerViewDTO getUserForOwner(String username) {
        return userMapper.toOwnerView(findUserByUsername(username));
    }

    @Transactional
    public void deletePhone(String username) {
        User user = findUserByUsername(username);
        user.setPhone(null);
        userRepository.save(user);
    }
}