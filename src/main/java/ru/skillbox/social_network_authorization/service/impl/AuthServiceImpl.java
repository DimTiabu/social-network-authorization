package ru.skillbox.social_network_authorization.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.mail.user}")
    private String mailUsername;
    @Value("${app.mail.password}")
    private String mailPassword;

    public TokenResponse authenticate(AuthenticateRq request) {
        User user = findUserByEmail(request.getEmail());

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

        String jwt = jwtServiceImpl.generateJwtToken(userDetails);
        log.info("Сгенерирован jwt: " + jwt);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        log.info("Сгенерирован refreshToken: " + refreshToken);

        return new TokenResponse(jwt, refreshToken.getToken());
    }

    @Transactional
    public String sendRecoveryEmail(RecoveryPasswordLinkRq request) {

        User user = findUserByEmail(request.getEmail());

        // Логика отправки письма с использованием SMTP
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.host", "smtp.mail.ru"); // Замените на ваш SMTP-сервер
        prop.put("mail.smtp.port", "587"); // Замените на порт вашего SMTP-сервера
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword); // Замените на ваши учётные данные
            }
        });

        ClassPathResource resource = new ClassPathResource("templates/recovery_email.html");

        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String htmlContent = reader.lines().collect(Collectors.joining("\n"));

            // Подстановка значения в HTML
            htmlContent = htmlContent.replace("%temp%", request.getTemp());

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername)); // Ваш e-mail
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(request.getEmail()));
            message.setSubject("Восстановление пароля");

            // Устанавливаем HTML-содержимое письма
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);

            log.info("Письмо успешно отправлено на адрес: " + request.getEmail());

        } catch (MessagingException | IOException e) {
            log.error("Ошибка при отправке письма: " + e.getMessage());
            return "ERROR";
        }

        user.setToken(request.getTemp());
        userRepository.save(user);

        return "OK";
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
    public String changePassword(ChangePasswordRq changePasswordRq, AppUserDetails userDetails) {
        User user = findUserByEmail(userDetails.getUsername());
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
    public String changeEmail(String email, AppUserDetails userDetails) {
        User user = findUserByEmail(userDetails.getUsername());
        user.setEmail(email);
        userRepository.save(user);
        return "Электронная почта успешно изменена";
    }

    @Override
    public String requestChangeEmailLink(String request, AppUserDetails userDetails) {
        return "";
    }

    @Override
    public String requestChangePasswordLink(String request) {
        return "";
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));
    }
}
