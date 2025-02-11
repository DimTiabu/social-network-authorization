package com.example.social_network_authorization.mapper;

import com.example.social_network_authorization.exception.PasswordsDoNotMatchException;
import com.example.social_network_authorization.entity.User;
import com.example.social_network_authorization.web.model.RegistrationDto;
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
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .token(registrationDto.getToken())
                .build();
    }
}
