package ru.tyabutov.social_network_authorization.repository;

import ru.tyabutov.social_network_authorization.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAccountId(UUID accountId);

    Optional<User> findByEmailAndChatId(String email, Long chatId);
}
