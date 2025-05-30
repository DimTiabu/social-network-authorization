package ru.tyabutov.social_network_authorization.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tyabutov.social_network_authorization.dto.AuthenticateRq;
import ru.tyabutov.social_network_authorization.dto.ChangePasswordRq;
import ru.tyabutov.social_network_authorization.dto.RecoveryPasswordLinkRq;
import ru.tyabutov.social_network_authorization.dto.TokenResponse;
import ru.tyabutov.social_network_authorization.entity.RefreshToken;
import ru.tyabutov.social_network_authorization.entity.User;
import ru.tyabutov.social_network_authorization.exception.EntityNotFoundException;
import ru.tyabutov.social_network_authorization.exception.InvalidPasswordException;
import ru.tyabutov.social_network_authorization.exception.PasswordsDoNotMatchException;
import ru.tyabutov.social_network_authorization.repository.UserRepository;
import ru.tyabutov.social_network_authorization.security.AppUserDetails;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
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
        ReflectionTestUtils.setField(authService, "mailUsername", "mailUsername");
        ReflectionTestUtils.setField(authService, "mailPassword", "mailPassword");

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void givenValidCredentials_whenAuthenticate_thenReturnTokenResponse() {
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
    void givenInvalidPassword_whenAuthenticate_thenThrowInvalidPasswordException() {
        AuthenticateRq request = new AuthenticateRq("test@example.com", "wrong_password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.authenticate(request, null));
    }
    @Test
    void givenUnknownUser_whenAuthenticate_thenThrowEntityNotFoundException() {
        AuthenticateRq request = new AuthenticateRq("unknown@example.com", "password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.authenticate(request, null));
    }

    @Test
    void givenValidCredentialsAndTelegramChatId_whenAuthenticate_thenReturnTokenResponse() {
        // Подготовка данных
        AuthenticateRq request = new AuthenticateRq("test@example.com", "password");
        String telegramChatId = "12345"; // Указываем chatId
        UUID accountId = UUID.randomUUID();
        user.setAccountId(accountId);
        user.setChatId(Long.valueOf(telegramChatId));

        AppUserDetails userDetails = new AppUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Мокаем поведение репозитория для поиска по email и chatId
        when(userRepository.findByEmailAndChatId(anyString(), anyLong())).thenReturn(Optional.of(user));
        when(jwtService.generateJwtToken(any(AppUserDetails.class))).thenReturn("accessToken123");
        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(RefreshToken.builder()
                .accountId(accountId)
                .token("refreshToken456")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build());

        // Устанавливаем аутентификацию в SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Выполнение
        TokenResponse response = authService.authenticate(request, telegramChatId);

        // Проверка
        assertNotNull(response);
        assertEquals("accessToken123", response.getAccessToken());
        assertEquals("refreshToken456", response.getRefreshToken());

        // Проверка, что пользователь найден по email и chatId
        verify(userRepository).findByEmailAndChatId(anyString(), anyLong());
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenUnknownUserAndTelegramChatId_whenAuthenticate_thenThrowEntityNotFoundException() {

        // Подготовка данных
        AuthenticateRq request = new AuthenticateRq("unknown@example.com", "password");
        String telegramChatId = "12345"; // Указываем chatId
        UUID accountId = UUID.randomUUID();
        user.setAccountId(accountId);

        when(userRepository.findByEmailAndChatId(anyString(), anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.authenticate(request, telegramChatId));
    }

    @Test
    void givenValidUserWithoutChatId_whenAuthenticate_thenCreateChatIdAndAuthenticate() {

        // Подготовка данных
        AuthenticateRq request = new AuthenticateRq("unknown@example.com", "password");
        String telegramChatId = "12345"; // Указываем chatId
        UUID accountId = UUID.randomUUID();
        user.setAccountId(accountId);

        AppUserDetails userDetails = new AppUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Мокаем поведение репозитория для поиска по email и chatId
        when(userRepository.findByEmailAndChatId(anyString(), anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateJwtToken(any(AppUserDetails.class))).thenReturn("accessToken123");
        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(RefreshToken.builder()
                .accountId(accountId)
                .token("refreshToken456")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build());

        // Выполнение
        authService.authenticate(request, telegramChatId);


        // Проверка, что пользователь не был найден по email и chatId, но создан новый
        verify(userRepository).findByEmailAndChatId(anyString(), anyLong());
        verify(userRepository).save(any(User.class)); // Проверка, что пользователь сохранен
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenExistingUser_whenSendRecoveryEmail_thenReturnSuccess(){
        // Подготовка данных
        RecoveryPasswordLinkRq request = new RecoveryPasswordLinkRq("test@example.com");

        // Мокаем поведение репозитория для поиска пользователя
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTemporaryPassword");

        // Мокаем Transport.send, чтобы не отправлять реальное письмо
        // Используем PowerMock для мока статических методов, если это необходимо
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null); // Можно использовать thenAnswer, чтобы "поймать" вызов метода

            // Выполнение метода
            String result = authService.sendRecoveryEmail(request);

            // Проверка
            assertEquals("OK", result);
            verify(userRepository).save(any(User.class)); // Проверка, что пароль был обновлен
        }
    }

    @Test
    void givenUnknownUser_whenSendRecoveryEmail_thenThrowEntityNotFoundException() {
        RecoveryPasswordLinkRq request = new RecoveryPasswordLinkRq("notfound@example.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.sendRecoveryEmail(request));
    }

    @Test
    void givenMailError_whenSendRecoveryEmail_thenReturnError() {
        RecoveryPasswordLinkRq request = new RecoveryPasswordLinkRq("test@example.com");

        // Мокаем поведение репозитория для поиска пользователя
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // Мокаем ошибку при отправке письма
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenThrow(new MessagingException("Sending email failed"));

            // Выполнение метода
            String result = authService.sendRecoveryEmail(request);

            // Проверка, что возвращается ошибка
            assertEquals("ERROR", result);
        }
    }

    @Test
    void givenValidOldPassword_whenChangePassword_thenReturnSuccessMessage() {
        ChangePasswordRq request = new ChangePasswordRq("newPass", "newPass", "oldPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword1())).thenReturn("encodedNewPass");

        String result = authService.changePassword(request, user.getEmail());
        assertEquals("Пароль успешно изменен", result);
    }

    @Test
    void givenInvalidOldPassword_whenChangePassword_thenReturnErrorMessage() {
        ChangePasswordRq request = new ChangePasswordRq("wrongPass", "newPass", "newPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        String result = authService.changePassword(request, user.getEmail());
        assertEquals("ERROR: Неверный старый пароль", result);
    }

    @Test
    void givenMismatchedNewPasswords_whenChangePassword_thenThrowPasswordsDoNotMatchException() {
        ChangePasswordRq request = new ChangePasswordRq("oldPass", "newPass", "newPass");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        // Лямбда, которая вызывает метод changePassword
        Executable executable = () -> authService.changePassword(request, user.getEmail());

        // Проверяем, что будет выброшено исключение PasswordsDoNotMatchException
        assertThrows(PasswordsDoNotMatchException.class, executable);
    }

    @Test
    void givenValidNewEmail_whenChangeEmail_thenReturnSuccessMessageAndSendKafkaMessage() {
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