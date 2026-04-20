package com.example.demo.mapper;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.TopUpFormDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = MapperUtils.class)
public interface UserMapper {

    @Mappings({
            @Mapping(target = "purchasesCount",
                    source = "user.purchases",
                    qualifiedByName = "sizeToLong"),
            @Mapping(target = "ratingsCount",
                    source = "user.ratings",
                    qualifiedByName = "sizeToLong"),
    })
    UserForOwnerViewDTO toOwnerView(User user);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "passwordHash",
                    source = "rawPassword", qualifiedByName = "rawToEncoded"),
            @Mapping(target = "fullName",
                    expression = "java(mapperUtils.toFullName(registerFormDTO.surname(), registerFormDTO.name(), registerFormDTO.patronymic()))"),
            @Mapping(target = "phone", source = "phone"),
            @Mapping(target = "balance", ignore = true),
            @Mapping(target = "purchases", ignore = true),
            @Mapping(target = "favourites", ignore = true),
            @Mapping(target = "ratings", ignore = true),
            @Mapping(target = "cartItems", ignore = true)
    })
    User fromRegisterForm(RegisterFormDTO registerFormDTO);

    void updateUserFromDto(ProfileSettingsDTO profileSettingsDTO, @MappingTarget User existingUser);

    @Mapping(target = "phone", expression = "java(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone())")
    ProfileSettingsDTO toSettingsForm(User user);

    @Mapping(target = "balance", source = "amount", qualifiedByName = "fromAmountToBalance")
    void updateFromTopUpForm(TopUpFormDTO topUpFormDTO, @MappingTarget User user);

    WalletForOwnerViewDTO toWalletView(User user);
}