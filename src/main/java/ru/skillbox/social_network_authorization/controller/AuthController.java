package ru.skillbox.social_network_authorization.controller;

import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService databaseAuthService;

    @PostMapping("/login")
    public String login(@RequestBody AuthenticateRq request) {
        return databaseAuthService.authenticate(request);
    }

    @PostMapping("/password/recovery")
    public String recoverPassword(@RequestBody RecoveryPasswordLinkRq request) {
        return databaseAuthService.sendRecoveryEmail(request);
    }

    @PostMapping("/password/recovery/{recoveryLink}")
    public String setNewPassword(@PathVariable String recoveryLink,
                                 @RequestBody SetPasswordRq request) {
        return databaseAuthService.updatePassword(recoveryLink, request);
    }

}
