package ru.skillbox.social_network_authorization.web.controller.impl;

import org.springframework.beans.factory.annotation.Value;
import ru.skillbox.social_network_authorization.mapper.UserMapper;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import ru.skillbox.social_network_authorization.web.model.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_authorization.web.model.kafka.RegistrationEventDto;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService databaseRegistrationService;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, RegistrationEventDto> kafkaTemplate;
    @Value("${app.kafka.topicProducer}")
    private String topicName;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegistrationDto registrationDto) {
        databaseRegistrationService.registerUser(
                userMapper.registrationDtoToUser(registrationDto));

        kafkaTemplate.send(topicName,
                RegistrationEventDto.builder()
                        .email(registrationDto.getEmail())
                        .firstName(registrationDto.getFirstName())
                        .lastName(registrationDto.getLastName())
                        .build());

        return ResponseEntity.ok("Успешная регистрация");
    }
}
