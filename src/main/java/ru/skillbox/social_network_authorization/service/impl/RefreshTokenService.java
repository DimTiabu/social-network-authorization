package ru.skillbox.social_network_authorization.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;
import ru.skillbox.social_network_authorization.repository.RefreshTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
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

    public RefreshToken checkRefreshToken(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(),
                    "Refresh token was expired. Repeat sign in action!");
        }

        return token;
    }

    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}