package ru.skillbox.social_network_authorization.controller;

import ru.skillbox.social_network_authorization.mapper.UserMapper;
import ru.skillbox.social_network_authorization.service.KafkaMessageService;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService databaseRegistrationService;
    private final UserMapper userMapper;
    private final KafkaMessageService kafkaMessageService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegistrationDto registrationDto) {
        databaseRegistrationService.registerUser(
                userMapper.registrationDtoToUser(registrationDto));

        kafkaMessageService.sendMessageWithUserData(registrationDto);

        return ResponseEntity.ok("Успешная регистрация");
    }
}
