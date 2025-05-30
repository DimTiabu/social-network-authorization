package ru.tyabutov.social_network_authorization.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.UUID;

@RedisHash("refresh_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @Indexed
    private UUID id;

    @Indexed
    private UUID accountId;

    @Indexed
    private String token;

    @Indexed
    private Instant expiryDate;
}