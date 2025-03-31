package ru.skillbox.social_network_authorization.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.skillbox.social_network_authorization.dto.kafka.CreatedAccountEventDto;
import ru.skillbox.social_network_authorization.dto.kafka.EmailChangedEventDto;
import ru.skillbox.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.skillbox.social_network_authorization.dto.kafka.UserOnlineEventDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaMessageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaMessageService kafkaMessageService;

    private RegistrationEventDto registrationEventDto;
    private UserOnlineEventDto userOnlineEventDto;
    private EmailChangedEventDto emailChangedEventDto;
    private CreatedAccountEventDto createdAccountEventDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationEventDto = new RegistrationEventDto(
                UUID.randomUUID(), "testUser", "FirstName", "LastName");
        userOnlineEventDto = new UserOnlineEventDto(String.valueOf(UUID.randomUUID()));
        emailChangedEventDto = new EmailChangedEventDto(UUID.randomUUID(), "new-email@example.com");
        createdAccountEventDto = new CreatedAccountEventDto(UUID.randomUUID(), UUID.randomUUID());

        user = new User();
        user.setId(createdAccountEventDto.getUserId());

        ReflectionTestUtils.setField(kafkaMessageService, "registrationTopic", "registration-topic");
        ReflectionTestUtils.setField(kafkaMessageService, "userOnlineTopic", "user-is-online-topic");
        ReflectionTestUtils.setField(kafkaMessageService, "changedEmailTopic", "changed-email-topic");
    }

    @Test
    void sendMessageWithUserData_shouldSendToKafka() {
        kafkaMessageService.sendMessageWithUserData(registrationEventDto);

        verify(kafkaTemplate, times(1)).send(anyString(), eq(registrationEventDto));
    }

    @Test
    void sendMessageWhenUserOnline_shouldSendToKafka() {
        kafkaMessageService.sendMessageWhenUserOnline(userOnlineEventDto);

        verify(kafkaTemplate, times(1)).send(anyString(), eq(userOnlineEventDto));
    }

    @Test
    void sendMessageWhenEmailIsChange_shouldSendToKafka() {
        kafkaMessageService.sendMessageWhenEmailIsChange(emailChangedEventDto);

        verify(kafkaTemplate, times(1)).send(anyString(), eq(emailChangedEventDto));
    }

    @Test
    void setAccountId_shouldSetAccountIdAndSaveUser() {
        when(userRepository.findById(createdAccountEventDto.getUserId())).thenReturn(Optional.of(user));

        kafkaMessageService.setAccountId(createdAccountEventDto);

        verify(userRepository, times(1)).findById(createdAccountEventDto.getUserId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void setAccountId_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(createdAccountEventDto.getUserId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> kafkaMessageService.setAccountId(createdAccountEventDto));

        verify(userRepository, times(1)).findById(createdAccountEventDto.getUserId());
        verify(userRepository, never()).save(any());
    }
}