package ru.skillbox.social_network_authorization.mapper;

import lombok.experimental.UtilityClass;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.PasswordsDoNotMatchException;

@UtilityClass
public final class UserMapperFactory {

    public User registrationDtoToUser(RegistrationDto registrationDto) {

        if (!registrationDto.getPassword1().equals(registrationDto.getPassword2())) {
            throw new PasswordsDoNotMatchException();
        }

        return User.builder()
                .email(registrationDto.getEmail())
                .password(registrationDto.getPassword1())
                .token(registrationDto.getToken())
                .build();
    }
}
