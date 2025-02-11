package com.example.social_network_authorization.service;

import org.springframework.stereotype.Service;

@Service
public interface CaptchaService {

    String generateCaptcha();
}
