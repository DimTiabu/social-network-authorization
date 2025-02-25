package ru.skillbox.social_network_authorization.controller;

import ru.skillbox.social_network_authorization.dto.CaptchaDto;
import ru.skillbox.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaServiceImpl;

    @GetMapping("/captcha")
    public CaptchaDto generateCaptcha() {
        return captchaServiceImpl.generateCaptcha();
    }
}
