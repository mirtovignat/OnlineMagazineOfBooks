package com.example.demo.service;

import com.example.demo.dto.authorize.LoginFormDTO;
import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.authorize.AlreadyRegisteredException;
import com.example.demo.exception.authorize.InvalidPasswordException;
import com.example.demo.exception.authorize.PasswordsMismatchException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthorizeService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    public void validateRegister(RegisterFormDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("RegisterFormDTO не может быть null");
        }
        if (!dto.rawPassword().equals(dto.repeatRawPassword())) {
            throw new PasswordsMismatchException();
        }
        if (userRepository.existsByUsername(dto.username()) ||
                userRepository.existsByPhone(dto.phone()) ||
                userRepository.existsByEmail(dto.email())) {
            throw new AlreadyRegisteredException();
        }
    }

    @Transactional
    public UserForOwnerViewDTO register(RegisterFormDTO dto) {
        User user = userMapper.fromRegisterForm(dto);
        user = userRepository.save(user);
        return userMapper.toOwnerView(user);

    }

    public User validateLogin(LoginFormDTO dto) {
        User user = switch (dto.identifier()) {
            case EMAIL -> userRepository.findByEmailOrThrow(dto.identifierValue());
            case PHONE -> userRepository.findByPhoneOrThrow(dto.identifierValue());
            case USERNAME -> userRepository.findByUsernameOrThrow(dto.identifierValue());
        };
        if (!passwordEncoder.matches(dto.rawPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }
        return user;
    }

    @Transactional
    public UserForOwnerViewDTO login(User user) {
        return new UserForOwnerViewDTO(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getBalance(),
                user.getCurrencyCode(),
                user.getPurchases().size(),
                user.getRatings().size()
        );
    }
}