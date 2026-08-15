package com.example.demo.mapper;

import com.example.demo.dto.authorize.RegisterFormDTO;
import com.example.demo.dto.user.ProfileSettingsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.dto.wallet.WalletForOwnerViewDTO;
import com.example.demo.model.User;
import com.example.demo.normalizer.PhoneNormalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = MapperUtils.class,
        imports = {PhoneNormalizer.class})
public abstract class UserMapper {

    public abstract UserForOwnerViewDTO toOwnerView(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "passwordHash", source = "rawPassword", qualifiedByName = "rawToEncoded")
    @Mapping(target = "fullName", expression = "java(mapperUtils.toFullName(registerFormDTO.surname(), registerFormDTO.name(), registerFormDTO.patronymic()))")
    @Mapping(target = "phone", expression = "java(PhoneNormalizer.normalizePhone(registerFormDTO.phone()))")
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "purchases", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    public abstract User fromRegisterForm(RegisterFormDTO registerFormDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "currencyCode", ignore = true)
    @Mapping(target = "purchases", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "phone", expression = "java(PhoneNormalizer.normalizePhone(profileSettingsDTO.phone()))")
    public abstract void updateUserFromDto(ProfileSettingsDTO profileSettingsDTO,
                                           @MappingTarget User existingUser);

    @Mapping(target = "phone", expression = "java(PhoneNormalizer.normalizePhone(user.getPhone()))")
    public abstract ProfileSettingsDTO toSettingsForm(User user);

    public abstract WalletForOwnerViewDTO toWalletView(User user);

    public ProfileSettingsDTO createProfileSettingsDTO(String username, String email, String phone) {
        return new ProfileSettingsDTO(username, email, PhoneNormalizer.normalizePhone(phone));
    }
}