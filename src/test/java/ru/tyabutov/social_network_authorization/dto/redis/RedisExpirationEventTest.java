package ru.tyabutov.social_network_authorization.dto.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import ru.tyabutov.social_network_authorization.entity.RefreshToken;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class RedisExpirationEventTest {

    private RedisExpirationEvent redisExpirationEvent;

    @BeforeEach
    void setUp() {
        redisExpirationEvent = new RedisExpirationEvent();
    }

    @Test
    void givenExpiredToken_whenHandleRedisKeyExpiredEvent_thenTokenProcessed() {
        // Создаем мок-объект RefreshToken
        RefreshToken expiredToken = new RefreshToken();
        UUID tokenId = UUID.randomUUID();
        expiredToken.setId(tokenId);
        expiredToken.setToken("expired-refresh-token");

        // Создаем мок-событие RedisKeyExpiredEvent с истекшим токеном
        @SuppressWarnings("unchecked")
        RedisKeyExpiredEvent<RefreshToken> event = (RedisKeyExpiredEvent<RefreshToken>) mock(RedisKeyExpiredEvent.class);
        when(event.getValue()).thenReturn(expiredToken);

        // Выполняем обработку события
        redisExpirationEvent.handleRedisKeyExpiredEvent(event);

        // Проверяем, что объект токена содержит корректные данные
        assertThat(expiredToken.getId()).isEqualTo(tokenId);
        assertThat(expiredToken.getToken()).isEqualTo("expired-refresh-token");
    }

    @Test
    void givenNullToken_whenHandleRedisKeyExpiredEvent_thenHandledGracefully() {
        // Создаем мок-событие RedisKeyExpiredEvent с null
        @SuppressWarnings("unchecked")
        RedisKeyExpiredEvent<RefreshToken> event = (RedisKeyExpiredEvent<RefreshToken>) mock(RedisKeyExpiredEvent.class);
        when(event.getValue()).thenReturn(null);

        assertDoesNotThrow(() -> redisExpirationEvent.handleRedisKeyExpiredEvent(event));
    }
}