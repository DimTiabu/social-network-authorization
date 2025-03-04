package ru.skillbox.social_network_authorization.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.dto.CaptchaDto;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.service.CaptchaService;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaServiceImpl;

    public void registerUser(User user, String code) {

//        CaptchaDto captcha = captchaServiceImpl.generateCaptcha();
        // Проверка капчи: сравниваем значение, введённое пользователем (code),
        // с сгенерированным значением (token)
//        if (!code.equalsIgnoreCase(captcha.getToken())) {
//            throw new RuntimeException("Неверно введена капча");
//        }

        String email = user.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EntityNotFoundException(
                    "Пользователь с электронной почтой " + email + " уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Удалить следующую строку, когда подключится сервис Дарьи
        user.setAccountId(UUID.randomUUID());

        userRepository.save(user);
    }
}
