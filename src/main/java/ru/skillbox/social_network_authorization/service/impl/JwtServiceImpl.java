package ru.skillbox.social_network_authorization.service.impl;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.JwtService;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    public String generateJwtToken(AppUserDetails userDetails) {
        String jwt = Jwts.builder()
                .claim("sub", userDetails.getUsername())
                .claim("accountId", userDetails.getId())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .setHeaderParam("typ", "JWT")
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        log.info("jwt: " + jwt);
        return jwt;
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validate(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
        } catch (SignatureException e) {
            log.info("Недопустимая подпись: " + e.getMessage());
            throw new SignatureException("Недопустимая подпись: " + e.getMessage());
        } catch (MalformedJwtException e) {
            log.info("Недопустимый токен: " + e.getMessage());
            throw new MalformedJwtException("Недопустимый токен: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info("Токен не поддерживается: " + e.getMessage());
            throw new UnsupportedJwtException("Токен не поддерживается: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("Токен просрочен");
            throw new ru.skillbox.social_network_authorization.exception.ExpiredJwtException();
        }
        return true;
    }
}
