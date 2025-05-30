package ru.tyabutov.social_network_authorization.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.tyabutov.social_network_authorization.dto.kafka.EmailChangedEventDto;
import ru.tyabutov.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.tyabutov.social_network_authorization.dto.kafka.UserOnlineEventDto;
import ru.tyabutov.social_network_authorization.entity.User;
import ru.tyabutov.social_network_authorization.repository.UserRepository;
import ru.tyabutov.social_network_authorization.dto.kafka.CreatedAccountEventDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${app.kafka.topicProducerRegistration}")
    private String registrationTopic;
    @Value("${app.kafka.topicProducerUserIsOnline}")
    private String userOnlineTopic;
    @Value("${app.kafka.topicProducerChangedEmail}")
    private String changedEmailTopic;

    public void sendMessageWithUserData(RegistrationEventDto registrationEventDto) {
        log.info("registrationEventDto: {}", registrationEventDto);
        kafkaTemplate.send(registrationTopic, registrationEventDto);
    }

    public void sendMessageWhenUserOnline(UserOnlineEventDto userOnlineEventDto) {
        log.info("Пользователь с accountId {} онлайн", userOnlineEventDto.getAccountId());
        kafkaTemplate.send(userOnlineTopic, userOnlineEventDto);
    }

    public void sendMessageWhenEmailIsChange(EmailChangedEventDto emailChangedEventDto) {
        log.info("Пользователь с accountId {} поменял email на {}",
                emailChangedEventDto.getAccountId(), emailChangedEventDto.getEmail());
        kafkaTemplate.send(changedEmailTopic, emailChangedEventDto);
    }

    public void setAccountId(CreatedAccountEventDto createdAccountEventDto) {

        User user = userRepository.findById(createdAccountEventDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не зарегистрирован"));

        user.setAccountId(createdAccountEventDto.getAccountId());
        userRepository.save(user);
    }
}