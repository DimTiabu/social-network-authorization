package ru.tyabutov.social_network_authorization.service;

import ru.tyabutov.social_network_authorization.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request, String telegramChatId);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String changePassword(ChangePasswordRq changePasswordRq, String email);

    String changeEmail(String email, String currentEmail);

}