package ru.tyabutov.social_network_authorization.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.tyabutov.social_network_authorization.entity.InvitationCode;
import ru.tyabutov.social_network_authorization.exception.EntityNotFoundException;
import ru.tyabutov.social_network_authorization.entity.User;
import ru.tyabutov.social_network_authorization.exception.InvalidInvitationCodeException;
import ru.tyabutov.social_network_authorization.repository.InvitationCodeRepository;
import ru.tyabutov.social_network_authorization.repository.UserRepository;
import ru.tyabutov.social_network_authorization.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user, String confirmationCode) {
        String email = user.getEmail();

        // Проверка существования пользователя
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EntityNotFoundException(
                    "Пользователь с электронной почтой " + email + " уже существует");
        }

        // Проверка инвайт-кода
        InvitationCode invitation = invitationCodeRepository
                .findByEmailAndConfirmationCode(email, confirmationCode)
                .orElseThrow(() -> new InvalidInvitationCodeException(
                        "Неверный пригласительный код или email"));

        if (invitation.isUsed()) {
            throw new InvalidInvitationCodeException("Пригласительный код уже использован");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInvitationCodeException("Пригласительный код просрочен");
        }

        // Установка пароля и сохранение пользователя
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Удаление использованного инвайт-кода
        invitationCodeRepository.delete(invitation);

        return savedUser;
    }
}
