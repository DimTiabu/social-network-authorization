package ru.skillbox.social_network_authorization.service;

import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.CaptchaDto;

@Service
public interface CaptchaService {

    CaptchaDto generateCaptcha();
}