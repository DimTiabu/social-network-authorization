package ru.skillbox.social_network_authorization.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ru.skillbox.social_network_authorization.dto.CaptchaDto;
import ru.skillbox.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class CaptchaController {

    private final CaptchaService captchaServiceImpl;

    @GetMapping("/captcha")
    public CaptchaDto generateCaptcha(HttpSession session) {
        CaptchaDto captchaDto = captchaServiceImpl.generateCaptcha();
        // Сохраняем сгенерированный токен в сессии
        log.info("captchaDto.getSecret = " + captchaDto.getSecret());
        session.setAttribute("captchaSecret", captchaDto.getSecret());
        return captchaDto;
    }
}
