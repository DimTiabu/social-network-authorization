package ru.skillbox.social_network_authorization.listener;

import ru.skillbox.social_network_authorization.mapper.CreationEventMapper;
import ru.skillbox.social_network_authorization.service.impl.KafkaMessageService;
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

    @KafkaListener(topics = "${app.kafka.topicListener}",
            groupId = "${app.kafka.kafkaMessageGroupId}",
            containerFactory = "kafkaMessageConcurrentKafkaListenerContainerFactory")
    public void listen(@Payload String creation) {

        kafkaMessageService.setAccountId(creationEventMapper.mapFromJson(creation));
    }
}
