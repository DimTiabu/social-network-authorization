package ru.skillbox.social_network_authorization.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.skillbox.social_network_authorization.dto.*;
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

    @PostMapping("/password/recovery/{recoveryLink}")
    public String setNewPassword(@PathVariable String recoveryLink,
                                 @RequestBody SetPasswordRq request) {
        return authService.updatePassword(recoveryLink, request);
    }

    @GetMapping("/validate")
    public Boolean validateToken(@RequestParam String token){
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
    public String logoutUser(){
        return refreshTokenService.logout();
    }

    // Новый эндпоинт для изменения пароля (Authenticated user)
    @PutMapping("/password")
    public String changePassword(@RequestBody String request,
                                 @AuthenticationPrincipal AppUserDetails userDetails) {
        return authService.changePassword(
                requestMapper.mapChangePasswordRqFromString(request), userDetails);
    }

    // Новый эндпоинт для изменения email (Authenticated user)
    @PutMapping("/email")
    public String changeEmail(@RequestBody String email) {
        return authService.changeEmail(email);
    }

    // Новый эндпоинт для запроса ссылки на изменение email (Authenticated user)
    @PostMapping("/change-email-link")
    public String requestChangeEmailLink(@RequestBody Map<String, Map<String, String>> payload) {
        String email = payload.get("email").get("email");
        log.info("email: " + email);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        log.info(currentEmail);
        return authService.requestChangeEmailLink(email, currentEmail);
    }


    // Новый эндпоинт для запроса ссылки на изменение пароля
    @PostMapping("/change-password-link")
    public String requestChangePasswordLink(@RequestBody String request,
                                            @RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        log.info("refreshToken: " + refreshToken);

        AppUserDetails userDetails = refreshTokenService.getUserByRefreshToken(refreshToken);
        return authService.requestChangePasswordLink(
                requestMapper.mapChangePasswordRqFromString(request), userDetails);
    }
}