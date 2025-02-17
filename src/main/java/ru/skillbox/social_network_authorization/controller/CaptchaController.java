package ru.skillbox.social_network_authorization.controller;

import ru.skillbox.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService databaseCaptchaService;

    @GetMapping("/captcha")
    public String generateCaptcha() {
        return databaseCaptchaService.generateCaptcha();
    }
}
