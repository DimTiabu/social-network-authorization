package ru.tyabutov.social_network_authorization.service;

import ru.tyabutov.social_network_authorization.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface RegistrationService {
    User registerUser(User user);
}