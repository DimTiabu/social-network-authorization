package ru.skillbox.social_network_authorization.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.security.jwt.JwtUtils;
import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.service.RefreshTokenService;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseAuthService implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String authenticate(AuthenticateRq request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        System.out.println("refreshToken " + refreshToken);

        String jwt = jwtUtils.generateJwtToken(userDetails);
        System.out.println("jwt " + jwt);
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
