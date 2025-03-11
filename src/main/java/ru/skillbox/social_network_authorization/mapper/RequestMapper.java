package ru.skillbox.social_network_authorization.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.dto.ChangePasswordRq;
import ru.skillbox.social_network_authorization.exception.MappingException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestMapper {

    private final ObjectMapper objectMapper;

    public ChangePasswordRq mapChangePasswordRqFromString(String request) {
        try {
            return objectMapper.readValue(request, ChangePasswordRq.class);
        } catch (JsonProcessingException e) {
            log.error("Ошибка обработки JSON при изменении пароля");
            throw new MappingException();
        }
    }
}
