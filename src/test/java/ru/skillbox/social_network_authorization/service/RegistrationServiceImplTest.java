package ru.skillbox.social_network_authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.service.impl.RegistrationServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("password")
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = registrationService.registerUser(user);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> registrationService.registerUser(user));

        assertEquals("Пользователь с электронной почтой test@example.com уже существует",
                exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}