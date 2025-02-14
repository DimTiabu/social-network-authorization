package ru.skillbox.social_network_authorization.web.controller.impl;

import ru.skillbox.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService databaseCaptchaService;

    @GetMapping("/captcha")
    public ResponseEntity<?> generateCaptcha() {
        return ResponseEntity.ok(databaseCaptchaService.generateCaptcha());
    }
}
