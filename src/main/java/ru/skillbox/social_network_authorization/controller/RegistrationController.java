package ru.skillbox.social_network_authorization.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ru.skillbox.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.CaptchaException;
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
@Slf4j
public class RegistrationController {
    private final RegistrationService registrationService;
    private final KafkaMessageService kafkaMessageService;

    @PostMapping("/register")
    public String register(
            @RequestBody @Valid RegistrationDto registrationDto,
            HttpSession session) {
        String expectedCaptcha = (String) session.getAttribute("captchaSecret");

        if (!expectedCaptcha.equalsIgnoreCase(registrationDto.getCaptchaCode())) {
            throw new CaptchaException("Капча введена неверно");
        }

        User user = registrationService.registerUser(
                UserMapperFactory.registrationDtoToUser(registrationDto));

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
