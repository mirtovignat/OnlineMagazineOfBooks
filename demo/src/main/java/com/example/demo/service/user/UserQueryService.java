package com.example.demo.service.user;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.mapper.entity.UserMapper;
import com.example.demo.model.entity.User;
import com.example.demo.repository.entity.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse buildUserPage(UserForOwnerViewDTO userForOwner) {
        Long userId = userForOwner.id();
        long purchasedCounts = getPurchasesCount(userId);
        long ratingsCount = getRatingsCount(userId);
        return UserResponse.builder().userForOwner(userForOwner)
                .purchasesCount(purchasedCounts)
                .ratingsCount(ratingsCount).build();
    }

    public BigDecimal getBalance(Long userId) {
        return getUser(userId).getBalance();
    }

    public User getUser(Long userId) {
        return userRepository.findByIdOrThrow(userId);
    }

    @Transactional(readOnly = true)
    public long getPurchasesCount(Long userId) {
        return userRepository.countPurchasesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getRatingsCount(Long userId) {
        return userRepository.countRatingsByUserId(userId);
    }

    public String getUsername(Long userId) {
        return getUser(userId).getUsername();
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

}
