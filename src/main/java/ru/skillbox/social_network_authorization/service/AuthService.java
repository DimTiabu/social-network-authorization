package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.SetPasswordRq;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    String authenticate(AuthenticateRq request);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);
}
