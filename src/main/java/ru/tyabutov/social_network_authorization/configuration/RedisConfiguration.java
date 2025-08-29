package ru.tyabutov.social_network_authorization.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.lang.NonNull;
import ru.tyabutov.social_network_authorization.entity.RefreshToken;

import java.time.Duration;
import java.util.Collections;

@Configuration
@EnableRedisRepositories(
        keyspaceConfiguration =
                RedisConfiguration.RefreshTokenKeyspaceConfiguration.class,
        enableKeyspaceEvents =
                RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfiguration {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration();

        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());

        return new LettuceConnectionFactory(configuration);
    }


    public class RefreshTokenKeyspaceConfiguration
            extends KeyspaceConfiguration {
        private static final String REFRESH_TOKEN_KEYSPACE =
                "refresh_tokens";

        @Override
        @NonNull
        protected Iterable<KeyspaceSettings> initialConfiguration() {
            KeyspaceSettings keyspaceSettings =
                    new KeyspaceSettings(
                            RefreshToken.class, REFRESH_TOKEN_KEYSPACE);

            keyspaceSettings.setTimeToLive(
                    refreshTokenExpiration.getSeconds());

            return Collections.singleton(keyspaceSettings);
        }
    }
}
