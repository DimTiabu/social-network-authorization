package ru.tyabutov.social_network_authorization.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tyabutov.social_network_authorization.dto.kafka.CreatedAccountEventDto;
import ru.tyabutov.social_network_authorization.exception.MappingException;

@Component
@RequiredArgsConstructor
public class CreationEventMapper {

    private final ObjectMapper objectMapper;

    public CreatedAccountEventDto mapFromJson(String creation) {
        try {
            return objectMapper.readValue(creation, CreatedAccountEventDto.class);
        } catch (JsonProcessingException e) {
            throw new MappingException();
        }
    }
}
