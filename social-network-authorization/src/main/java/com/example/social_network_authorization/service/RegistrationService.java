package com.example.social_network_authorization.service;

import com.example.social_network_authorization.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface RegistrationService {
    User registerUser(User user);
}
