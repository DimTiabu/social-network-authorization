package ru.tyabutov.social_network_authorization.listener;

import ru.tyabutov.social_network_authorization.dto.kafka.CreatedAccountEventDto;
import ru.tyabutov.social_network_authorization.mapper.CreationEventMapper;
import ru.tyabutov.social_network_authorization.service.impl.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final KafkaMessageService kafkaMessageService;
    private final CreationEventMapper creationEventMapper;

    @KafkaListener(topics = "${app.kafka.topicListenerCreatedAccount}",
            groupId = "${app.kafka.kafkaMessageGroupId}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload String creation) {

        log.info("creation: {}", creation);
        CreatedAccountEventDto createdAccountEventDto = creationEventMapper.mapFromJson(creation);

        log.info("Создан аккаунт с userId {}и accountId {}", createdAccountEventDto.getUserId(), createdAccountEventDto.getAccountId());
        kafkaMessageService.setAccountId(createdAccountEventDto);
    }
}
