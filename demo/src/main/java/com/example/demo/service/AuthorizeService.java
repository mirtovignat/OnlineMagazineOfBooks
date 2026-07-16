package com.example.demo.service;

import com.example.demo.dto.authorize.LoginFormDTO;
import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AuthorizeService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User validateLogin(LoginFormDTO loginFormDTO) {
        User user = switch (loginFormDTO.identifier()) {
            case USERNAME -> userRepository.findByUsername(loginFormDTO.identifierValue())
                    .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, loginFormDTO.identifierValue()));
            case EMAIL -> userRepository.findByEmail(loginFormDTO.identifierValue())
                    .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, loginFormDTO.identifierValue()));
            case PHONE -> userRepository.findByPhone(loginFormDTO.identifierValue())
                    .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND, loginFormDTO.identifierValue()));
        };

        if (!passwordEncoder.matches(loginFormDTO.rawPassword(), user.getPasswordHash())) {
            throw BusinessException.of(ErrorCode.PASSWORD_INVALID);
        }
        return user;
    }

    public void validateRegister(RegisterFormDTO registerFormDTO) {
        registerFormDTO.ifMismatch();
        if (userRepository.existsByUsername(registerFormDTO.username())) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }
        if (userRepository.existsByEmail(registerFormDTO.email())) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }
        if (registerFormDTO.phone() != null && !registerFormDTO.phone().isBlank() && userRepository.existsByPhone(registerFormDTO.phone())) {
            throw BusinessException.of(ErrorCode.ALREADY_REGISTERED);
        }
    }

    @Transactional
    public UserForOwnerViewDTO register(RegisterFormDTO registerFormDTO) {
        User user = userMapper.fromRegisterForm(registerFormDTO);
        user.setBalance(BigDecimal.ZERO);
        user.setDeleted(false);
        userRepository.save(user);
        return userMapper.toOwnerView(user);
    }

    public UserForOwnerViewDTO login(User user) {
        return userMapper.toOwnerView(user);
    }
}