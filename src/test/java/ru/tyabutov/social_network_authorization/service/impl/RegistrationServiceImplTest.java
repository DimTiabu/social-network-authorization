package ru.tyabutov.social_network_authorization.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tyabutov.social_network_authorization.entity.InvitationCode;
import ru.tyabutov.social_network_authorization.entity.User;
import ru.tyabutov.social_network_authorization.exception.EntityNotFoundException;
import ru.tyabutov.social_network_authorization.exception.InvalidInvitationCodeException;
import ru.tyabutov.social_network_authorization.repository.InvitationCodeRepository;
import ru.tyabutov.social_network_authorization.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private User user;
    private InvitationCode validInvitationCode;
    private final String confirmationCode = "INVITE123";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password("password")
                .build();

        validInvitationCode = InvitationCode.builder()
                .id(UUID.randomUUID())
                .email(email)
                .confirmationCode(confirmationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isUsed(false)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(invitationCodeRepository.findByEmailAndConfirmationCode(email, confirmationCode))
                .thenReturn(Optional.of(validInvitationCode));
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = registrationService.registerUser(user, confirmationCode);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository).save(any(User.class));
        verify(invitationCodeRepository).delete(validInvitationCode);
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> registrationService.registerUser(user, confirmationCode));

        assertEquals("Пользователь с электронной почтой test@example.com уже существует",
                exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(invitationCodeRepository, never()).delete(any());
    }

    @Test
    void registerUser_InvalidInvitationCode_ThrowsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(invitationCodeRepository.findByEmailAndConfirmationCode(email, confirmationCode))
                .thenReturn(Optional.empty());

        InvalidInvitationCodeException exception = assertThrows(InvalidInvitationCodeException.class,
                () -> registrationService.registerUser(user, confirmationCode));

        assertEquals("Неверный пригласительный код или email", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(invitationCodeRepository, never()).delete(any());
    }

    @Test
    void registerUser_InvitationCodeAlreadyUsed_ThrowsException() {
        InvitationCode usedInvitationCode = InvitationCode.builder()
                .id(UUID.randomUUID())
                .email(email)
                .confirmationCode(confirmationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isUsed(true)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(invitationCodeRepository.findByEmailAndConfirmationCode(email, confirmationCode))
                .thenReturn(Optional.of(usedInvitationCode));

        InvalidInvitationCodeException exception = assertThrows(InvalidInvitationCodeException.class,
                () -> registrationService.registerUser(user, confirmationCode));

        assertEquals("Пригласительный код уже использован", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(invitationCodeRepository, never()).delete(any());
    }

    @Test
    void registerUser_ExpiredInvitationCode_ThrowsException() {
        InvitationCode expiredInvitationCode = InvitationCode.builder()
                .id(UUID.randomUUID())
                .email(email)
                .confirmationCode(confirmationCode)
                .createdAt(LocalDateTime.now().minusDays(8))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isUsed(false)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(invitationCodeRepository.findByEmailAndConfirmationCode(email, confirmationCode))
                .thenReturn(Optional.of(expiredInvitationCode));

        InvalidInvitationCodeException exception = assertThrows(InvalidInvitationCodeException.class,
                () -> registrationService.registerUser(user, confirmationCode));

        assertEquals("Пригласительный код просрочен", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(invitationCodeRepository, never()).delete(any());
    }

    @Test
    void registerUser_EmailMismatch_ThrowsException() {
        User userWithDifferentEmail = User.builder()
                .id(UUID.randomUUID())
                .email("different@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail(userWithDifferentEmail.getEmail())).thenReturn(Optional.empty());
        when(invitationCodeRepository.findByEmailAndConfirmationCode("different@example.com", confirmationCode))
                .thenReturn(Optional.empty());

        InvalidInvitationCodeException exception = assertThrows(InvalidInvitationCodeException.class,
                () -> registrationService.registerUser(userWithDifferentEmail, confirmationCode));

        assertEquals("Неверный пригласительный код или email", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(invitationCodeRepository, never()).delete(any());
    }
}