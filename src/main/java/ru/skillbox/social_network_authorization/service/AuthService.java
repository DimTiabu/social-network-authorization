package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String changePassword(ChangePasswordRq changePasswordRq, String email);

    String changeEmail(String email);

    String requestChangeEmailLink(String email, String currentEmail);

    String requestChangePasswordLink(ChangePasswordRq changePasswordRq, String email);
}