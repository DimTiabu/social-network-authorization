package ru.skillbox.social_network_authorization.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.exception.RefreshTokenException;
import ru.skillbox.social_network_authorization.repository.RefreshTokenRepository;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

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

    private final JwtServiceImpl jwtService;

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

    public void checkRefreshToken(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(),
                    "Refresh token was expired. Repeat sign in action!");
        }
    }

    public void deleteByAccountId(UUID accountId) {
        refreshTokenRepository.deleteByAccountId(accountId);
    }

    public TokenResponse refreshTokens(String refreshToken, AppUserDetails userDetails) {
        RefreshToken storedRefreshToken = findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException(refreshToken, "Invalid refresh token!"));

        checkRefreshToken(storedRefreshToken);

        // Генерируем новые токены
        String newAccessToken = jwtService.generateJwtToken(userDetails);
        RefreshToken newRefreshToken = createRefreshToken(userDetails.getId());

        // Удаляем старый refreshToken
        refreshTokenRepository.delete(storedRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken.getToken());
    }

    public String logout(){
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserDetails userDetails){
            UUID accountId = userDetails.getId();

            deleteByAccountId(accountId);

        }
        return "Успешный выход из аккаунта";
    }
}