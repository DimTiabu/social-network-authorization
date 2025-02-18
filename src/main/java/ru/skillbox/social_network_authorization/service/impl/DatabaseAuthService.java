package ru.skillbox.social_network_authorization.service.impl;

import org.springframework.beans.factory.annotation.Value;
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

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class DatabaseAuthService implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.mail.user}")
    private String mailUsername;
    @Value("${app.mail.password}")
    private String mailPassword;

    public String authenticate(AuthenticateRq request) {
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

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        System.out.println("refreshToken " + refreshToken);

        String jwt = jwtUtils.generateJwtToken(userDetails);
        System.out.println("jwt " + jwt);
        return "Успешный вход";
    }

    public String sendRecoveryEmail(RecoveryPasswordLinkRq request) {
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
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername)); // Замените на ваш адрес электронной почты
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(request.getEmail()));
            message.setSubject("Восстановление пароля");

            String recoveryUrl = "http://91.197.98.213/api/v1/auth/password/recovery/" + request.getTemp();
            message.setText("Ваша ссылка для восстановления пароля: " + recoveryUrl);
            Transport.send(message);

            System.out.println("Письмо успешно отправлено на адрес: " + request.getEmail());

        } catch (MessagingException e) {
            System.err.println("Ошибка при отправке письма: " + e.getMessage());
            return "ERROR";
        }

        User user = findUserByEmail(request.getEmail());
        user.setToken(request.getTemp());
        userRepository.save(user);

        return "OK";
    }

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

    private User findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь не зарегистрирован"));
    }
}
