package ru.skillbox.social_network_authorization.listener;

import ru.skillbox.social_network_authorization.service.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.skillbox.social_network_authorization.dto.kafka.CreatedAccountEventDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final KafkaMessageService kafkaMessageService;

    @KafkaListener(topics = "${app.kafka.topicListener}",
            groupId = "${app.kafka.kafkaMessageGroupId}",
            containerFactory = "kafkaMessageConcurrentKafkaListenerContainerFactory")
    public void listen(@Payload CreatedAccountEventDto createdAccountEventDto) {

        kafkaMessageService.setAccountId(createdAccountEventDto);
    }
}
