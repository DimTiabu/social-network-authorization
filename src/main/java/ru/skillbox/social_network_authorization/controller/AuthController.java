package ru.skillbox.social_network_authorization.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_authorization.service.JwtService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthenticateRq request) {
        return authService.authenticate(request);
    }

    @PostMapping("/password/recovery")
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
    public TokenResponse refreshToken(@RequestParam RefreshToken refreshToken,
                                      @AuthenticationPrincipal AppUserDetails userDetails){
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
        return authService.changePassword(request, userDetails);
    }

    // Новый эндпоинт для изменения email (Authenticated user)
    @PutMapping("/email")
    public String changeEmail(@RequestBody String request,
                              @AuthenticationPrincipal AppUserDetails userDetails) {
        return authService.changeEmail(request, userDetails);
    }

    // Новый эндпоинт для запроса ссылки на изменение email (Authenticated user)
    @PostMapping("/change-email-link")
    public String requestChangeEmailLink(@RequestBody String request,
                                         @AuthenticationPrincipal AppUserDetails userDetails) {
        return authService.requestChangeEmailLink(request, userDetails);
    }

    // Новый эндпоинт для запроса ссылки на изменение пароля
    @PostMapping("/change-password-link")
    public String requestChangePasswordLink(@RequestBody String request) {
        return authService.requestChangePasswordLink(request);
    }
}