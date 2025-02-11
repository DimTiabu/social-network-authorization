package com.example.social_network_authorization.web.controller.impl;

import com.example.social_network_authorization.mapper.UserMapper;
import com.example.social_network_authorization.service.RegistrationService;
import com.example.social_network_authorization.service.impl.DatabaseRegistrationService;
import com.example.social_network_authorization.web.model.RegistrationDto;
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

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegistrationDto registrationDto) {
        databaseRegistrationService.registerUser(
                userMapper.registrationDtoToUser(registrationDto));

        return ResponseEntity.ok("Успешная регистрация");
    }
}
