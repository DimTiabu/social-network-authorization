package ru.skillbox.social_network_authorization.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import ru.skillbox.social_network_authorization.dto.*;
import ru.skillbox.social_network_authorization.exception.JwtAuthenticationException;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_authorization.service.JwtService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthenticateRq request,
                               @RequestHeader(value = "Telegram", required = false) UUID telegramHeader) {
        log.info("Запущен метод login");

        return authService.authenticate(request, telegramHeader);
    }

    @PostMapping("/password/recovery/")
    public String recoverPassword(@RequestBody RecoveryPasswordLinkRq request) {
        log.info("Запущен метод recoverPassword");

        return authService.sendRecoveryEmail(request);
    }

    @GetMapping("/validate")
    public Boolean validateToken(@RequestParam String token) {
        log.info("Запущен метод validateToken");

        return jwtService.validate(token);
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestBody Map<String, String> payload) {
        log.info("Запущен метод refreshToken");

        String refreshToken = payload.get("refreshToken");
        log.info("refreshToken: {}", refreshToken);

        AppUserDetails userDetails = refreshTokenService.getUserByRefreshToken(refreshToken);

        return refreshTokenService.refreshTokens(refreshToken, userDetails);
    }

    @PostMapping("/logout")
    public String logoutUser() {
        log.info("Запущен метод logoutUser");

        return refreshTokenService.logout();
    }

    @PostMapping("/change-email-link")
    public String requestChangeEmail(@RequestBody Map<String, Map<String, String>> payload,
                                     HttpServletRequest request) {
        log.info("Запущен метод requestChangeEmail");

        String email = payload.get("email").get("email");
        log.info("email: {}", email);

        String token = getToken(request);

        String currentEmail = jwtService.getUsername(token);
        log.info(currentEmail);
        return authService.changeEmail(email, currentEmail);
    }

    @PostMapping("/change-password-link")
    public String requestChangePassword(@RequestBody Map<String, String> payload,
                                        HttpServletRequest request) {
        log.info("Запущен метод requestChangePassword");

        String newPassword1 = payload.get("newPassword1");
        log.info("newPassword1: {}", newPassword1);
        String newPassword2 = payload.get("newPassword2");
        log.info("newPassword2: {}", newPassword2);
        String oldPassword = payload.get("oldPassword");
        log.info("oldPassword: {}", oldPassword);

        String token = getToken(request);

        String currentEmail = jwtService.getUsername(token);

        ChangePasswordRq changePasswordRq = new ChangePasswordRq(newPassword1, newPassword2, oldPassword);
        return authService.changePassword(changePasswordRq, currentEmail);
    }

    private String getToken(HttpServletRequest request) throws JwtAuthenticationException {

        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            log.info("HeaderAuth: {}", headerAuth);
            return headerAuth.substring(7);
        } else {
            throw new JwtAuthenticationException("JWT token is missing or invalid");
        }
    }

}