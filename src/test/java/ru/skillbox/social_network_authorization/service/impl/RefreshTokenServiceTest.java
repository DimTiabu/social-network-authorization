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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
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

    private final Duration refreshTokenExpiration = Duration.ofMinutes(30);

    private UUID accountId;
    private RefreshToken refreshToken;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", Duration.ofHours(2));
        accountId = UUID.randomUUID();
        user = new User();
        user.setAccountId(accountId);
        user.setEmail("test@example.com");

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

//    @Test
//    void givenValidTokenButNoUser_WhenExceptionThrown_ThenCallLogout() {
//        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
//        when(userRepository.findByAccountId(refreshToken.getAccountId())).thenReturn(Optional.empty());
//
//        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
//            SecurityContext securityContext = mock(SecurityContext.class);
//            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
//            Authentication authentication = mock(Authentication.class);
//            when(securityContext.getAuthentication()).thenReturn(authentication);
//            AppUserDetails userDetails = mock(AppUserDetails.class);
//            when(authentication.getPrincipal()).thenReturn(userDetails);
//            assertThrows(EntityNotFoundException.class, () -> refreshTokenService.getUserByRefreshToken(token));
//            verify(securityContext).setAuthentication(null);
//        }
//    }


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
        AppUserDetails userDetails = new AppUserDetails(user);
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        refreshToken.setExpiryDate(Instant.now().minusSeconds(10));
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(user));

        assertThrows(RefreshTokenException.class, () -> refreshTokenService.checkRefreshToken(refreshToken));

        SecurityContextHolder.clearContext();
    }
}