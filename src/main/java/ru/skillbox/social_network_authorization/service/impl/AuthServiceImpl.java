package ru.skillbox.social_network_authorization.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_authorization.dto.AuthenticateResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
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

    public AuthenticateResponse authenticate(AuthenticateRq request) {
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

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new AuthenticateResponse(jwt, refreshToken.getToken());
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
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword); // Замените на ваши учётные данные
            }
        });

        try {
            ClassPathResource resource = new ClassPathResource("templates/recovery_email.html");
            String htmlContent = new String(
                    Files.readAllBytes(resource.getFile().toPath()),
                    StandardCharsets.UTF_8);

            // Подстановка кода для восстановления пароля вместо %temp_password%
            htmlContent = htmlContent.replace("%temp%", request.getTemp());

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername)); // Замените на ваш адрес электронной почты
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(request.getEmail()));
            message.setSubject("Восстановление пароля");

            // Устанавливаем HTML-содержимое письма
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);

            System.out.println("Письмо успешно отправлено на адрес: " + request.getEmail());

        } catch (MessagingException | IOException e) {
            System.err.println("Ошибка при отправке письма: " + e.getMessage());
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
            System.err.println("Код восстановления не совпадает с ожидаемым");
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

        System.out.println("Пароль успешно обновлен для пользователя: " + user.getEmail());
        return "OK";
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));
    }
}
