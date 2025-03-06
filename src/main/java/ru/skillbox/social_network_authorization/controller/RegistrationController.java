package ru.skillbox.social_network_authorization.controller;

import ru.skillbox.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.mapper.UserMapperFactory;
import ru.skillbox.social_network_authorization.service.impl.KafkaMessageService;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final KafkaMessageService kafkaMessageService;

    @PostMapping("/register")
    public String register(
            @RequestBody @Valid RegistrationDto registrationDto) {
        User user = registrationService.registerUser(
                UserMapperFactory.registrationDtoToUser(registrationDto),
                registrationDto.getCode());

        kafkaMessageService.sendMessageWithUserData(
                RegistrationEventDto.builder()
                        .userId(user.getId())
                        .email(registrationDto.getEmail())
                        .firstName(registrationDto.getFirstName())
                        .lastName(registrationDto.getLastName())
                        .build()
        );

        return "Успешная регистрация";
    }
}
