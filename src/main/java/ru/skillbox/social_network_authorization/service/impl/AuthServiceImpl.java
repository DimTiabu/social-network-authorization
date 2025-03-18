package ru.skillbox.social_network_authorization.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.social_network_authorization.dto.*;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.exception.PasswordsDoNotMatchException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RestTemplate restTemplate;

    public TokenResponse authenticate(AuthenticateRq request) {
        User user = findUserByEmail(request.getEmail());
        log.info("passwordEncoder.encode(user.getToken()) - " + user.getToken());
        log.info("user.getToken() - " + user.getToken());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())
                && !request.getPassword().equals(user.getToken())) {
            throw new InvalidPasswordException();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        String jwt = jwtServiceImpl.generateJwtToken(userDetails);
        log.info("Сгенерирован jwt: " + jwt);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        log.info("Сгенерирован refreshToken: " + refreshToken);

        return new TokenResponse(jwt, refreshToken.getToken());
    }

    @Transactional
    public String sendRecoveryEmail(RecoveryPasswordLinkRq request) {

        User user = findUserByEmail(request.getEmail());

        // Логика отправки письма с использованием стороннего сервера
        String url = "http://212.192.20.30:45760/api/v1/email";

        String response = restTemplate.postForObject(url, request, String.class);
        if (Objects.equals(response, "OK")) {
            user.setToken(request.getTemp());
//            user.setPassword(request.getTemp());
            userRepository.save(user);
        }

        return response;
    }

    @Transactional
    public String updatePassword(String recoveryLink, SetPasswordRq request) {
// Сравниваем токен из URL с кодом, указанным пользователем (temp)
        if (!recoveryLink.equals(request.getTemp())) {
            log.error("Код восстановления не совпадает с ожидаемым");
            return "ERROR: Неверный код восстановления";
        }

        User user = userRepository.findByToken(recoveryLink)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не найден для токена: "
                                + recoveryLink));

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));
        user.setToken(null);
        userRepository.save(user);

        log.info("Пароль успешно обновлен для пользователя: " + user.getEmail());
        return "OK";
    }

    @Override
    public String changePassword(ChangePasswordRq changePasswordRq, String email) {
        User user = findUserByEmail(email);
        log.info("OldPassword: " + changePasswordRq.getOldPassword());
        log.info("NewPassword: " + changePasswordRq.getNewPassword1());

        if (!passwordEncoder.matches(changePasswordRq.getNewPassword1(), changePasswordRq.getNewPassword2())) {
            throw new PasswordsDoNotMatchException();
        }

        if (!passwordEncoder.matches(changePasswordRq.getOldPassword(), user.getPassword())) {
            log.error("Неверный старый пароль для пользователя: " + user.getEmail());
            return "ERROR: Неверный старый пароль";
        }

        user.setPassword(passwordEncoder.encode(changePasswordRq.getNewPassword1()));
        userRepository.save(user);

        log.info("Пароль успешно изменен для пользователя: " + user.getEmail());
        return "Пароль успешно изменен";
    }

    @Override
    public String changeEmail(String email) {
        return "";
    }

    @Override
    public String requestChangeEmailLink(String email, String currentEmail) {
        User user = findUserByEmail(currentEmail);
        user.setEmail(email);
        userRepository.save(user);
        return "Электронная почта успешно изменена";
    }

    @Override
    public String requestChangePasswordLink(ChangePasswordRq changePasswordRq, String email) {
        return changePassword(changePasswordRq, email);
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));
    }
}
