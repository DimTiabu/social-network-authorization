package com.example.social_network_authorization.web.controller.impl;

import com.example.social_network_authorization.service.CaptchaService;
import com.example.social_network_authorization.service.impl.DatabaseCaptchaService;
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
