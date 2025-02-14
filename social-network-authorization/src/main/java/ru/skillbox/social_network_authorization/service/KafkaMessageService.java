package ru.skillbox.social_network_authorization.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.repository.UserRepository;
import ru.skillbox.social_network_authorization.web.model.kafka.CreatedAccountEventDto;

@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final UserRepository userRepository;

    public void setAccountId(CreatedAccountEventDto createdAccountEventDto) {

        User user = userRepository.findById(createdAccountEventDto.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не зарегистрирован"));

        user.setAccountId(createdAccountEventDto.getAccountId());
    }
}
