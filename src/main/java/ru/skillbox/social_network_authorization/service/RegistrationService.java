package ru.skillbox.social_network_authorization.service;

import ru.skillbox.social_network_authorization.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface RegistrationService {
    void registerUser(User user, String code);
}
