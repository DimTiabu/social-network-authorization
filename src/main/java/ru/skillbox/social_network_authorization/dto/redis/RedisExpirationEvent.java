package ru.skillbox.social_network_authorization.dto.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;

@Component
@Slf4j
public class RedisExpirationEvent {

    @EventListener
    public void handleRedisKeyExpiredEvent(
            RedisKeyExpiredEvent<RefreshToken> event) {

        try {
            RefreshToken expiredRefreshToken = (RefreshToken) event.getValue();

            if (expiredRefreshToken == null) {
                throw new RefreshTokenException(
                        "Refresh token отсутствует в Redis");
            }

            log.info("Refresh token с ключом {} истек! Токен: {}", expiredRefreshToken.getId(), expiredRefreshToken.getToken());

        } catch (RefreshTokenException e) {
            log.error("Ошибка обработки события истечения RefreshToken: {}", e.getMessage());
        }
    }
}