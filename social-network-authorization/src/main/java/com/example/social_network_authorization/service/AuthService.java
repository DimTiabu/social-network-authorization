package com.example.social_network_authorization.service;

import com.example.social_network_authorization.web.model.AuthenticateRq;
import com.example.social_network_authorization.web.model.RecoveryPasswordLinkRq;
import com.example.social_network_authorization.web.model.SetPasswordRq;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String authenticate(AuthenticateRq request);

    String updatePassword(String recoveryLink, SetPasswordRq request);
}
