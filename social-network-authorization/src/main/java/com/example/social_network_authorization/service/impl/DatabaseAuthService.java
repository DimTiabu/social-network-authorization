package com.example.social_network_authorization.service.impl;

import com.example.social_network_authorization.entity.User;
import com.example.social_network_authorization.repository.UserRepository;
import com.example.social_network_authorization.service.AuthService;
import com.example.social_network_authorization.web.model.AuthenticateRq;
import com.example.social_network_authorization.web.model.RecoveryPasswordLinkRq;
import com.example.social_network_authorization.web.model.SetPasswordRq;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseAuthService implements AuthService {
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    public String authenticate(AuthenticateRq request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));

//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new InvalidPasswordException();
//        }
        // Логика аутентификации
        return "Успешный вход";
    }

    public String sendRecoveryEmail(RecoveryPasswordLinkRq request) {
        // Здесь нужно добавить логику отправки писем
        System.out.println("Отправляем письмо на: " + request.getEmail());
        System.out.println("Код восстановления: " + request.getTemp());
        return "OK";
    }

    public String updatePassword(String recoveryLink, SetPasswordRq request) {
        System.out.println("Обновление пароля");
        // Логика изменения пароля
        return "OK";
    }
}
