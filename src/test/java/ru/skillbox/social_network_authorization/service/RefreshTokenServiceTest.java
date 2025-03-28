package ru.skillbox.social_network_authorization.service;

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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;
import ru.skillbox.social_network_authorization.repository.RefreshTokenRepository;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

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

    private final Duration refreshTokenExpiration = Duration.ofMinutes(30);

    private UUID accountId;
    private RefreshToken refreshToken;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", Duration.ofHours(2));
        accountId = UUID.randomUUID();
        user = new User();
        user.setAccountId(accountId);
        user.setEmail("test@example.com");

        String jwtSecret = "testSecret";
        String token = Jwts.builder()
                .claim("accountId", accountId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        refreshToken = RefreshToken.builder()
                .accountId(accountId)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(token)
                .build();
    }

    @Test
    void shouldFindByRefreshToken() {
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        RefreshToken foundToken = refreshTokenService.findByRefreshToken(refreshToken.getToken());
        assertEquals(refreshToken.getToken(), foundToken.getToken());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenNotFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(user));
        String fakeValidToken = Jwts.builder()
                .claim("accountId", UUID.randomUUID()) // случайный несуществующий accountId
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(30).toMillis()))
                .signWith(SignatureAlgorithm.HS256, "testSecret") // тот же secret, что в сервисе
                .compact();

        assertThrows(RefreshTokenException.class, () -> refreshTokenService.findByRefreshToken(fakeValidToken));
    }

    @Test
    void shouldCreateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        RefreshToken createdToken = refreshTokenService.createRefreshToken(accountId);
        assertNotNull(createdToken);
        assertEquals(accountId, createdToken.getAccountId());
    }

    @Test
    void shouldDeleteByAccountId() {
        doNothing().when(refreshTokenRepository).deleteByAccountId(accountId);
        assertDoesNotThrow(() -> refreshTokenService.deleteByAccountId(accountId));
    }

    @Test
    void shouldThrowExceptionForExpiredRefreshToken() {
        // Устанавливаем фиктивного пользователя в SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        AppUserDetails userDetails = new AppUserDetails(user);
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        refreshToken.setExpiryDate(Instant.now().minusSeconds(10));
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(user));
        assertThrows(RefreshTokenException.class, () -> refreshTokenService.checkRefreshToken(refreshToken));

        SecurityContextHolder.clearContext();
    }
}