package ru.skillbox.social_network_authorization.service.impl;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.skillbox.social_network_authorization.dto.kafka.UserOnlineEventDto;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Mock
    private KafkaMessageService kafkaMessageService;

    private AppUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new AppUserDetails(
                UUID.randomUUID(),
                "testUser@example.com",
                "password",
                Collections.emptyList());
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "testSecret");
        ReflectionTestUtils.setField(jwtService, "tokenExpiration", Duration.ofHours(1));
    }

    @Test
    void givenUserDetails_whenGenerateJwtToken_thenShouldReturnValidToken() {
        String token = jwtService.generateJwtToken(userDetails);
        assertNotNull(token);
    }

    @Test
    void givenValidToken_whenGetUsername_thenShouldReturnCorrectUsername() {
        String token = jwtService.generateJwtToken(userDetails);
        String username = jwtService.getUsername(token);
        assertEquals("testUser@example.com", username);
    }

    @Test
    void givenValidToken_whenValidate_thenShouldReturnTrueAndSendKafkaMessage() {
        String token = jwtService.generateJwtToken(userDetails);
        assertTrue(jwtService.validate(token));
        verify(kafkaMessageService, times(1))
                .sendMessageWhenUserOnline(any(UserOnlineEventDto.class));
    }

    @Test
    void givenTokenWithInvalidSignature_whenValidate_thenShouldReturnFalse() {
        String token = jwtService.generateJwtToken(userDetails);

        ReflectionTestUtils.setField(jwtService, "jwtSecret", "invalidSecret");
        assertFalse(jwtService.validate(token));
    }

    @Test
    void givenMalformedToken_whenValidate_thenShouldReturnFalse() {
        assertFalse(jwtService.validate("invalidToken"));
    }

    @Test
    void givenUnsupportedJwtToken_whenValidate_thenShouldReturnFalse() {
        String unsupportedToken = Jwts.builder()
                .setSubject("user@example.com")
                .compact(); // Без подписи
        assertFalse(jwtService.validate(unsupportedToken));
    }

    @Test
    void givenExpiredToken_whenValidate_thenShouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "tokenExpiration", Duration.ofMillis(-1));
        String token = jwtService.generateJwtToken(userDetails);
        assertFalse(jwtService.validate(token));
    }
}