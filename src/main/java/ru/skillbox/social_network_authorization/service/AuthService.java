package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);

    String changePassword(String request, AppUserDetails userDetails);

    String changeEmail(String email, AppUserDetails userDetails);

    String requestChangeEmailLink(String request, AppUserDetails userDetails);

    String requestChangePasswordLink(String request);
}