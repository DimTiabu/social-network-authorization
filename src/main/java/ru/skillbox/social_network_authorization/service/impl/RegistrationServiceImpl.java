package ru.skillbox.social_network_authorization.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {

        String email = user.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EntityNotFoundException(
                    "Пользователь с электронной почтой " + email + " уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}