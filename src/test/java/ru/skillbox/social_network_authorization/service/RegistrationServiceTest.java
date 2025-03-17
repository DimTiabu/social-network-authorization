package ru.skillbox.social_network_authorization.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.service.impl.RegistrationServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Profile("test")
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void testSuccessfulUserRegistration() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        registrationService.registerUser(user);
    }

    @Test
    void testExistingEmailRegistration() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(EntityNotFoundException.class, () -> registrationService.registerUser(user));
    }
}

