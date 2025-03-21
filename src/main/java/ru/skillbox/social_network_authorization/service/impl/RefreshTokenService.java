package ru.skillbox.social_network_authorization.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;
import ru.skillbox.social_network_authorization.repository.RefreshTokenRepository;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final JwtServiceImpl jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException(refreshToken,
                        "Недействительный refresh-токен для пользователя " +
                                getEmailFromRefreshToken(refreshToken) + "!"));
    }

    public AppUserDetails getUserByRefreshToken(String refreshToken) {
        try {
            RefreshToken storedRefreshToken = findByRefreshToken(refreshToken);

            User user = findByAccountId(storedRefreshToken.getAccountId());

            log.info("Пользователь с email {} использует refresh-токен", user.getEmail());

            return new AppUserDetails(user);
        } catch (RefreshTokenException e) {
            logout();
            throw e;
        }
    }

    public RefreshToken createRefreshToken(UUID accountId) {
        String token = Jwts.builder()
                .claim("accountId", accountId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis()))
                .setHeaderParam("typ", "Refresh")
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        RefreshToken refreshToken = RefreshToken.builder()
                .accountId(accountId)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .token(token)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public void checkRefreshToken(RefreshToken token) {
        try {
            if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
                refreshTokenRepository.delete(token);
                throw new RefreshTokenException(token.getToken(),
                        "Срок действия refresh-токена истек. Авторизуйтесь повторно!");
            }
        } catch (RefreshTokenException e) {
            logout();
        }
    }

    public void deleteByAccountId(UUID accountId) {
        refreshTokenRepository.deleteByAccountId(accountId);
    }

    public TokenResponse refreshTokens(String refreshToken, AppUserDetails userDetails) {
        RefreshToken storedRefreshToken = findByRefreshToken(refreshToken);

        checkRefreshToken(storedRefreshToken);

        User user = findByAccountId(userDetails.getId());

        log.info("Генерация новых токенов для пользователя с email {}", user.getEmail());

        // Генерируем новые токены
        String newAccessToken = jwtService.generateJwtToken(userDetails);
        RefreshToken newRefreshToken = createRefreshToken(userDetails.getId());

        // Удаляем старый refreshToken
        refreshTokenRepository.delete(storedRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken.getToken());
    }

    public String logout() {
        log.info("Запуск метода logout");
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserDetails userDetails) {
            UUID accountId = userDetails.getId();

            User user = findByAccountId(accountId);
            log.info("Пользователь с email {} выходит из системы", user.getEmail());

            deleteByAccountId(accountId);
        }
        return "Успешный выход из аккаунта";
    }

    private String getEmailFromRefreshToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        UUID accountId = UUID.fromString(claims.get("accountId", String.class));

        User user = findByAccountId(accountId);

        return user.getEmail();
    }

    private User findByAccountId (UUID accountId){
        return userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не зарегистрирован!"));
    }
}