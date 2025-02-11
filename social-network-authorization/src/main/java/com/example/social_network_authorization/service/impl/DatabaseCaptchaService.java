package com.example.social_network_authorization.service.impl;

import com.example.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseCaptchaService implements CaptchaService {

    public String generateCaptcha() {
        //Генерация капчи
        return "Успешная генерация";
    }

}
