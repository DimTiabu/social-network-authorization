package com.example.social_network_authorization.web.controller.impl;

import com.example.social_network_authorization.service.AuthService;
import com.example.social_network_authorization.web.model.AuthenticateRq;
import com.example.social_network_authorization.web.model.RecoveryPasswordLinkRq;
import com.example.social_network_authorization.web.model.SetPasswordRq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService databaseAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticateRq request) {

        return ResponseEntity.ok(databaseAuthService.authenticate(request));
    }

    @PostMapping("/password/recovery")
    public ResponseEntity<?> recoverPassword(@RequestBody RecoveryPasswordLinkRq request) {
        return ResponseEntity.ok(databaseAuthService.sendRecoveryEmail(request));
    }

    @PostMapping("/password/recovery/{recoveryLink}")
    public ResponseEntity<?> setNewPassword(@PathVariable String recoveryLink,
                                            @RequestBody SetPasswordRq request) {
        return ResponseEntity.ok(databaseAuthService.updatePassword(recoveryLink, request));
    }

}
