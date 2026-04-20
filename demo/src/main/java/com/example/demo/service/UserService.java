package com.example.demo.service;

import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserMapper userMapper;
    @Autowired
    private final CartService cartService;

    @Transactional
    public void changeProfile(ProfileSettingsDTO profileSettingsDTO, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        userMapper.updateUserFromDto(profileSettingsDTO, user);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String username) {
        userRepository.deleteByUsername(username);
    }

    public UserForOwnerViewDTO getUserForOwner(String username) {
        return userMapper.toOwnerView(userRepository
                .findByUsernameOrThrow(username));
    }

    @Transactional
    public void topUp(TopUpFormDTO topUpFormDTO, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        WalletForOwnerViewDTO walletForOwnerViewDTO = userMapper.toWalletView(user);
        userMapper.updateFromTopUpForm(topUpFormDTO,
                userRepository.findByUsernameOrThrow(username));
        userRepository.save(user);
    }

    public WalletForOwnerViewDTO getWalletForOwner(String username) {
        return userMapper.toWalletView(userRepository.findByUsernameOrThrow(username));
    }
}