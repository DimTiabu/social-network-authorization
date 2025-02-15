package ru.skillbox.social_network_authorization.mapper;

import ru.skillbox.social_network_authorization.exception.PasswordsDoNotMatchException;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import org.springframework.stereotype.Component;

@Component
public abstract class UserMapperDelegate implements UserMapper {

    @Override
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
