package com.example.demo.mapper;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.PasswordChangingDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
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
                    source = "registerFormDTO.rawPassword", qualifiedByName = "rawToEncoded"),
            @Mapping(target = "fullName",
                    expression = "java(mapperUtils.toFullName(registerFormDTO.surname(), registerFormDTO.name(), registerFormDTO.patronymic()))"),
            @Mapping(
                    target = "phone",
                    expression = "java(registerFormDTO.phone() != null && !registerFormDTO.phone().trim().isEmpty() ? registerFormDTO.phone().trim() : null)"
            ),
            @Mapping(target = "balance", ignore = true),
            @Mapping(target = "purchases", ignore = true),
            @Mapping(target = "favourites", ignore = true),
            @Mapping(target = "ratings", ignore = true),
            @Mapping(target = "cartItems", ignore = true)
    })
    User fromRegisterForm(RegisterFormDTO registerFormDTO);

    @Mapping(
            target = "phone",
            expression = "java(profileSettingsDTO.phone() != null && !profileSettingsDTO.phone().trim().isEmpty() ? profileSettingsDTO.phone().trim() : null)"
    )
    void updateUserFromDto(ProfileSettingsDTO profileSettingsDTO, @MappingTarget User existingUser);

    @Mapping(target = "phone", expression = "java(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone().trim())")
    ProfileSettingsDTO toSettingsForm(User user);

    WalletForOwnerViewDTO toWalletView(User user);

    @Mapping(target = "currentPassword", ignore = true)
    @Mapping(target = "rawPassword", ignore = true)
    @Mapping(target = "repeatRawPassword", ignore = true)
    PasswordChangingDTO toPasswordChangingForm(User user);
}