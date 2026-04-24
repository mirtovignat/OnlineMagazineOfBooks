package com.example.demo.mapper;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.*;
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

    void updateUserFromUsernameChangingDTO(UsernameChangingDTO usernameChangingDTO,
                                           @MappingTarget User existingUser);

    void updateUserFromEmailChangingDTO(EmailChangingDTO emailChangingDTO,
                                        @MappingTarget User existingUser);

    void updateUserFromPhoneChangingDTO(PhoneChangingDTO phoneChangingDTO,
                                        @MappingTarget User existingUser);


    @Mapping(target = "phone", expression = "java(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone())")
    ProfileSettingsDTO toSettingsForm(User user);

    UsernameChangingDTO toUsernameChangingForm(User user);

    EmailChangingDTO toEmailChangingForm(User user);

    @Mapping(target = "phone", expression = "java(user.getPhone() == null || user.getPhone().isBlank() ? null : user.getPhone())")
    PhoneChangingDTO toPhoneChangingForm(User user);

    WalletForOwnerViewDTO toWalletView(User user);

    @Mapping(target = "currentPassword", ignore = true)
    @Mapping(target = "rawPassword", ignore = true)
    @Mapping(target = "repeatRawPassword", ignore = true)
    PasswordChangingDTO toPasswordChangingForm(User user);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "purchases", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currencyCode", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "passwordHash", source = "rawPassword", qualifiedByName = "rawToEncoded")
    void updateUserFromPasswordChangingDTO(PasswordChangingDTO dto, @MappingTarget User user);

}