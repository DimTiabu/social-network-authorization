package ru.skillbox.social_network_authorization.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.entity.RefreshToken;
import ru.skillbox.social_network_authorization.exception.MappingException;

@Component
@RequiredArgsConstructor
public class RefreshTokenMapper {

    private final ObjectMapper objectMapper;

    public RefreshToken mapFromJson(String refreshToken) {
        try {
            return objectMapper.readValue(refreshToken, RefreshToken.class);
        } catch (JsonProcessingException e) {
            throw new MappingException();
        }
    }
}
