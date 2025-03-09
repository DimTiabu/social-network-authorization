package ru.skillbox.social_network_authorization.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.dto.kafka.CreatedAccountEventDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, RegistrationEventDto> kafkaTemplate;
    @Value("${app.kafka.topicProducer}")
    private String producerTopicName;

    public void sendMessageWithUserData(RegistrationEventDto registrationEventDto) {
        log.info("registrationEventDto: " + registrationEventDto);
        kafkaTemplate.send(producerTopicName, registrationEventDto);
    }

    public void setAccountId(CreatedAccountEventDto createdAccountEventDto) {

        User user = userRepository.findById(createdAccountEventDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не зарегистрирован"));

        user.setAccountId(createdAccountEventDto.getAccountId());
        userRepository.save(user);
    }
}
