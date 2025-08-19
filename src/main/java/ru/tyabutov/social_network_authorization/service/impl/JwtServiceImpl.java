package ru.tyabutov.social_network_authorization.service.impl;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tyabutov.social_network_authorization.dto.kafka.UserOnlineEventDto;
import ru.tyabutov.social_network_authorization.security.AppUserDetails;
import ru.tyabutov.social_network_authorization.service.JwtService;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    private final KafkaMessageService kafkaMessageService;

    public String generateJwtToken(AppUserDetails userDetails) {
        String jwt = Jwts.builder()
                .setSubject(userDetails.getUsername()) // <- исправлено: теперь email как subject
                .claim("accountId", userDetails.getId().toString()) // UUID как строка
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis())) // оставлено как есть
                .setHeaderParam("typ", "JWT")
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        log.info("Создан jwt для пользователя {}", userDetails.getUsername());
        return jwt;
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // теперь точно возвращает email
    }

    public boolean validate(String authToken) {
        try {
            String email = getUsername(authToken);
            log.info("Запущен метод validate для пользователя {}", email);

            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(authToken)
                    .getBody();

            String accountId = claims.get("accountId", String.class); // безопасное приведение
            kafkaMessageService.sendMessageWhenUserOnline(new UserOnlineEventDto(accountId));

            return true;
        } catch (SignatureException e) {
            log.error("Недопустимая подпись: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Недопустимый токен: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Токен не поддерживается: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Токен просрочен");
        } catch (Exception e) {
            log.error("Ошибка при валидации токена: {}", e.getMessage());
        }
        return false;
    }
}
