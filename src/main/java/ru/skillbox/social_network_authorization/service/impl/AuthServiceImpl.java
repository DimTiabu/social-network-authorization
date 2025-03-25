package ru.skillbox.social_network_authorization.service.impl;

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
import ru.skillbox.social_network_authorization.dto.kafka.EmailChangedEventDto;
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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaMessageService kafkaMessageService;

    @Value("${app.mail.user}")
    private String mailUsername;
    @Value("${app.mail.password}")
    private String mailPassword;

    public TokenResponse authenticate(AuthenticateRq request, String telegramChatId) {
        if (telegramChatId != null) {
            // 1. Ищем пользователя по email и chatId
            Long chatId = Long.parseLong(telegramChatId);
            User currentUser = userRepository.findByEmailAndChatId(request.getEmail(), chatId).orElse(null);

            if (currentUser == null) {
                User user = validatePassword(request);
                user.setChatId(chatId);
                userRepository.save(user);
            } else {

                log.info("User authenticated via Telegram chatId, skipping password check");

                // 2. Принудительно устанавливаем аутентификацию (без проверки пароля)
                AppUserDetails userDetails = new AppUserDetails(currentUser);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } else {
            // Стандартная логика: ищем по email
            validatePassword(request);
        }

        // Генерация токенов
        AppUserDetails currentUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String jwt = jwtServiceImpl.generateJwtToken(currentUserDetails);
        log.info("Сгенерирован jwt: {}", jwt);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(currentUserDetails.getId());
        log.info("Сгенерирован refreshToken: {}", refreshToken);

        return new TokenResponse(jwt, refreshToken.getToken());
    }

    private User validatePassword(AuthenticateRq request) {
        User user = findUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // Аутентификация через authenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return user;
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
        prop.put("mail.smtp.connectiontimeout", "5000"); // Таймаут на подключение

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

            log.info("Письмо успешно отправлено на адрес: {}", request.getEmail());

        } catch (MessagingException | IOException e) {
            log.error("Ошибка при отправке письма: {}", e.getMessage());
            return "ERROR";
        }

        user.setPassword(passwordEncoder.encode(request.getTemp()));
        userRepository.save(user);

        return "OK";
    }

    @Override
    public String changePassword(ChangePasswordRq changePasswordRq, String email) {
        User user = findUserByEmail(email);
        log.info("OldPassword: {}", changePasswordRq.getOldPassword());
        log.info("NewPassword: {}", changePasswordRq.getNewPassword1());

        if (!passwordEncoder.matches(changePasswordRq.getOldPassword(), user.getPassword())) {
            log.error("Неверный старый пароль для пользователя: {}", user.getEmail());
            return "ERROR: Неверный старый пароль";
        }

        if (!changePasswordRq.getNewPassword1().equals(changePasswordRq.getNewPassword2())) {
            throw new PasswordsDoNotMatchException();
        }

        user.setPassword(passwordEncoder.encode(changePasswordRq.getNewPassword1()));
        userRepository.save(user);

        log.info("Пароль успешно изменен для пользователя: {}", user.getEmail());
        return "Пароль успешно изменен";
    }

    @Override
    public String changeEmail(String email, String currentEmail) {
        User user = findUserByEmail(currentEmail);
        user.setEmail(email);
        userRepository.save(user);
        kafkaMessageService.sendMessageWhenEmailIsChange(
                new EmailChangedEventDto(user.getAccountId(), email)
        );
        return "Электронная почта успешно изменена";
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));
    }
}
