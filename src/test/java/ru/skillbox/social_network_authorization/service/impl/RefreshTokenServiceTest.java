package ru.skillbox.social_network_authorization.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;
import ru.skillbox.social_network_authorization.repository.RefreshTokenRepository;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtServiceImpl jwtService;

    private final Duration refreshTokenExpiration = Duration.ofMinutes(30);

    private UUID accountId;
    private RefreshToken refreshToken;
    private User user;
    private String token;
    private RefreshToken expiredToken;
    private AppUserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", Duration.ofHours(2));
        accountId = UUID.randomUUID();
        user = new User();
        user.setAccountId(accountId);
        user.setEmail("test@example.com");

        userDetails = new AppUserDetails(user);

        token = Jwts.builder()
                .claim("accountId", accountId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS256, "testSecret")
                .compact();

        refreshToken = RefreshToken.builder()
                .accountId(accountId)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(token)
                .build();

        expiredToken = RefreshToken.builder()
                .accountId(accountId)
                .expiryDate(Instant.now().minusSeconds(10))
                .token(token)
                .build();
    }

    @Test
    void givenValidRefreshToken_WhenFindByRefreshToken_ThenReturnToken() {
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        RefreshToken foundToken = refreshTokenService.findByRefreshToken(refreshToken.getToken());
        assertEquals(refreshToken.getToken(), foundToken.getToken());
    }

    @Test
    void givenInvalidRefreshToken_WhenFindByRefreshToken_ThenThrowException() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(RefreshTokenException.class,
                () -> refreshTokenService.findByRefreshToken(token));
    }

    @Test
    void givenValidRefreshToken_WhenGetUserByRefreshToken_ThenReturnUserDetails() {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByAccountId(refreshToken.getAccountId())).thenReturn(Optional.of(user));

        AppUserDetails result = refreshTokenService.getUserByRefreshToken(token);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getUsername());
    }

    @Test
    void givenInvalidRefreshToken_WhenGetUserByRefreshToken_ThenThrowRefreshTokenException() {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenException.class, () -> refreshTokenService.getUserByRefreshToken(token));
    }

    @Test
    void givenValidRefreshToken_WhenUserNotFound_ThenThrowEntityNotFoundException() {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByAccountId(refreshToken.getAccountId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> refreshTokenService.getUserByRefreshToken(token));
    }

    @Test
    void givenAccountId_WhenCreateRefreshToken_ThenReturnNewToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        RefreshToken createdToken = refreshTokenService.createRefreshToken(accountId);

        assertNotNull(createdToken);
        assertEquals(accountId, createdToken.getAccountId());
    }

    @Test
    void givenAccountId_WhenDeleteRefreshToken_ThenNoExceptionThrown() {
        doNothing().when(refreshTokenRepository).deleteByAccountId(accountId);

        assertDoesNotThrow(() -> refreshTokenService.deleteByAccountId(accountId));
    }

    @Test
    void givenExpiredRefreshToken_WhenCheckRefreshToken_ThenThrowException() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        refreshToken.setExpiryDate(Instant.now().minusSeconds(10));
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(user));

        assertThrows(RefreshTokenException.class, () -> refreshTokenService.checkRefreshToken(refreshToken));

        SecurityContextHolder.clearContext();
    }

    @Test
    void checkRefreshToken_shouldThrowException_whenTokenExpired() {
        assertThrows(RefreshTokenException.class, () -> refreshTokenService.checkRefreshToken(expiredToken));
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void refreshTokens_shouldGenerateNewTokens_whenRefreshTokenValid() {
        when(refreshTokenRepository.findByToken("valid_token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));
        when(jwtService.generateJwtToken(any())).thenReturn("new_access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        TokenResponse response = refreshTokenService.refreshTokens("valid_token", userDetails);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void logout_shouldReturnMessage_whenUserNotAuthenticated() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        String message = refreshTokenService.logout();

        assertEquals("Выход невозможен – пользователь не аутентифицирован", message);
    }

    @Test
    void logout_shouldDeleteTokens_whenUserAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));

        String message = refreshTokenService.logout();

        assertEquals("Успешный выход из аккаунта", message);
        verify(refreshTokenRepository, times(1)).deleteByAccountId(accountId);
    }

}