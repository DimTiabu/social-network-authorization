package ru.skillbox.social_network_authorization.dto.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RedisExpirationEventTest {

    private RedisExpirationEvent redisExpirationEvent;

    @BeforeEach
    void setUp() {
        redisExpirationEvent = new RedisExpirationEvent();
    }

    @Test
    void givenExpiredToken_whenHandleRedisKeyExpiredEvent_thenLogMessage() {
        // Создаем мок-объект RefreshToken
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setId(UUID.randomUUID());
        expiredToken.setToken("expired-refresh-token");

        // Создаем мок-событие RedisKeyExpiredEvent с истекшим токеном
        RedisKeyExpiredEvent<RefreshToken> event = Mockito.mock(RedisKeyExpiredEvent.class);
        when(event.getValue()).thenReturn(expiredToken);

        // Захватываем логи (если требуется, можно использовать @Slf4j тестовую обертку)
        redisExpirationEvent.handleRedisKeyExpiredEvent(event);

        // Если нужна проверка логов, можно использовать LogCaptor или AOP-инструменты
    }

    @Test
    void givenNullToken_whenHandleRedisKeyExpiredEvent_thenThrowException() {
        // Создаем мок-событие RedisKeyExpiredEvent с null
        RedisKeyExpiredEvent<RefreshToken> event = Mockito.mock(RedisKeyExpiredEvent.class);
        when(event.getValue()).thenReturn(null);

        // Проверяем, что вызывается исключение RefreshTokenException
        assertThatThrownBy(() -> redisExpirationEvent.handleRedisKeyExpiredEvent(event))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessageContaining("Refresh token отсутствует в Redis");
    }
}