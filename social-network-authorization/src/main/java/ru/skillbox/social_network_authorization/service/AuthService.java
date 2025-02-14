package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.web.model.AuthenticateRq;
import ru.skillbox.social_network_authorization.web.model.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.web.model.SetPasswordRq;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String authenticate(AuthenticateRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);
}
