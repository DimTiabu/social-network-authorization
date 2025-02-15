package ru.skillbox.social_network_authorization.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.security.AppUserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateJwtToken(AppUserDetails userDetails){
       return Jwts.builder()
               .claim("sub", userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsername(String token){
        return Jwts.parser().setSigningKey(jwtSecret).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validate(String authToken){
        try {
            Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return  true;
        } catch (SignatureException e){
            log.error("Invalid signature: {}", e.getMessage());
        } catch (MalformedJwtException e){
            log.error("Invalid token: {}", e.getMessage());
        } catch (ExpiredJwtException e){
            log.error("Token is expired: {}", e.getMessage());
        }catch (UnsupportedJwtException e){
            log.error("Token is unsupported: {}", e.getMessage());
        }catch (IllegalArgumentException e){
            log.error("Claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
