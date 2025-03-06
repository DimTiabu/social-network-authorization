package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.TokenResponse;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);
}
