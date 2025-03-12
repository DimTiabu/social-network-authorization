package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.*;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);

    String changePassword(ChangePasswordRq changePasswordRq, AppUserDetails userDetails);

    String changeEmail(String email, String currentEmail);

    String requestChangeEmailLink(String email, String currentEmail);

    String requestChangePasswordLink(ChangePasswordRq changePasswordRq, AppUserDetails userDetails);
}