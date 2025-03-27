package ru.skillbox.social_network_authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.ChangePasswordRq;
import ru.skillbox.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.exception.PasswordsDoNotMatchException;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.impl.AuthServiceImpl;
import ru.skillbox.social_network_authorization.service.impl.JwtServiceImpl;
import ru.skillbox.social_network_authorization.service.impl.KafkaMessageService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaMessageService kafkaMessageService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void authenticate_Success() {
        // Подготовка данных
        AuthenticateRq request = new AuthenticateRq("test@example.com", "password");
        UUID accountId = UUID.randomUUID();
        user.setAccountId(accountId);

        RefreshToken refreshToken = RefreshToken.builder()
                .accountId(accountId)
                .token("refreshToken456")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        AppUserDetails userDetails = new AppUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateJwtToken(any(AppUserDetails.class))).thenReturn("accessToken123");
        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(refreshToken);

        // Устанавливаем аутентификацию в SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Выполнение
        TokenResponse response = authService.authenticate(request, null);

        // Проверка
        assertNotNull(response);
        assertEquals("accessToken123", response.getAccessToken());
        assertEquals("refreshToken456", response.getRefreshToken());

        // Очистка SecurityContext после теста
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticate_Fail_InvalidPassword() {
        AuthenticateRq request = new AuthenticateRq("test@example.com", "wrong_password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.authenticate(request, null));
    }
    @Test
    void authenticate_UserNotFound_ThrowsException() {
        AuthenticateRq request = new AuthenticateRq("unknown@example.com", "password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.authenticate(request, null));
    }

    @Test
    void sendRecoveryEmail_UserNotFound() {
        RecoveryPasswordLinkRq request = new RecoveryPasswordLinkRq("notfound@example.com", "tempPass");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.sendRecoveryEmail(request));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRq request = new ChangePasswordRq("newPass", "newPass", "oldPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword1())).thenReturn("encodedNewPass");

        String result = authService.changePassword(request, user.getEmail());
        assertEquals("Пароль успешно изменен", result);
    }

    @Test
    void changePassword_InvalidOldPassword() {
        ChangePasswordRq request = new ChangePasswordRq("wrongPass", "newPass", "newPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        String result = authService.changePassword(request, user.getEmail());
        assertEquals("ERROR: Неверный старый пароль", result);
    }

    @Test
    void changePassword_PasswordsDoNotMatch() {
        ChangePasswordRq request = new ChangePasswordRq("oldPass", "newPass", "newPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        assertThrows(PasswordsDoNotMatchException.class,
                () -> authService.changePassword(request, user.getEmail()));
    }

    @Test
    void changeEmail_Success() {
        String newEmail = "new@example.com";
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Мокаем вызов KafkaMessageService, чтобы избежать NPE
        doNothing().when(kafkaMessageService).sendMessageWhenEmailIsChange(any());

        String result = authService.changeEmail(newEmail, user.getEmail());
        assertEquals("Электронная почта успешно изменена", result);

        // Проверяем, что KafkaMessageService действительно вызван
        verify(kafkaMessageService, times(1)).sendMessageWhenEmailIsChange(any());
    }
}