package ru.skillbox.social_network_authorization.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import ru.skillbox.social_network_authorization.dto.*;
import ru.skillbox.social_network_authorization.exception.JwtAuthenticationException;
import ru.skillbox.social_network_authorization.mapper.RequestMapper;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_authorization.service.JwtService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final RequestMapper requestMapper;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthenticateRq request) {
        return authService.authenticate(request);
    }

    @PostMapping("/password/recovery/")
    public String recoverPassword(@RequestBody RecoveryPasswordLinkRq request) {
        return authService.sendRecoveryEmail(request);
    }

    @GetMapping("/validate")
    public Boolean validateToken(@RequestParam String token) {
        return jwtService.validate(token);
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        log.info("refreshToken: " + refreshToken);

        AppUserDetails userDetails = refreshTokenService.getUserByRefreshToken(refreshToken);

        return refreshTokenService.refreshTokens(refreshToken, userDetails);
    }


    @PostMapping("/logout")
    public String logoutUser() {
        return refreshTokenService.logout();
    }

    // Новый эндпоинт для изменения пароля (Authenticated user)
    @PutMapping("/password")
    public String changePassword(@RequestBody String request, String email) {
        return authService.changePassword(
                requestMapper.mapChangePasswordRqFromString(request), email);
    }

    // Новый эндпоинт для изменения email (Authenticated user)
    @PutMapping("/email")
    public String changeEmail(@RequestBody String email) {
        return authService.changeEmail(email);
    }

    // Новый эндпоинт для запроса ссылки на изменение email (Authenticated user)
    @PostMapping("/change-email-link")
    public String requestChangeEmailLink(@RequestBody Map<String, Map<String, String>> payload,
                                         HttpServletRequest request) {
        String email = payload.get("email").get("email");
        log.info("email: " + email);

        String token = getToken(request);

        String currentEmail = jwtService.getUsername(token);
        log.info(currentEmail);
        return authService.requestChangeEmailLink(email, currentEmail);
    }


    // Новый эндпоинт для запроса ссылки на изменение пароля
    @PostMapping("/change-password-link")
    public String requestChangePasswordLink(@RequestBody Map<String, String> payload,
                                            HttpServletRequest request) {
        String newPassword1 = payload.get("newPassword1");
        log.info("newPassword1: " + newPassword1);
        String newPassword2 = payload.get("newPassword2");
        log.info("newPassword2: " + newPassword2);
        String oldPassword = payload.get("oldPassword");
        log.info("oldPassword: " + oldPassword);

        String token = getToken(request);

        String currentEmail = jwtService.getUsername(token);
        log.info(currentEmail);

        ChangePasswordRq changePasswordRq = new ChangePasswordRq(newPassword1, newPassword2, oldPassword);
        return authService.requestChangePasswordLink(changePasswordRq, currentEmail);
    }

    private String getToken(HttpServletRequest request) throws JwtAuthenticationException {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            log.info("HeaderAuth: " + headerAuth);
            return headerAuth.substring(7);
        } else {
            throw new JwtAuthenticationException("JWT token is missing or invalid");
        }
    }

}