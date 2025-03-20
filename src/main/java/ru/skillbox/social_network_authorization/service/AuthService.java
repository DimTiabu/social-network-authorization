package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.dto.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface AuthService {
    TokenResponse authenticate(AuthenticateRq request, UUID telegramChatId);

    String sendRecoveryEmail(RecoveryPasswordLinkRq request);

    String changePassword(ChangePasswordRq changePasswordRq, String email);

    String changeEmail(String email, String currentEmail);

}