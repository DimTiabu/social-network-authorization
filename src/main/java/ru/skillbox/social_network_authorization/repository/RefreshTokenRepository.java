package ru.skillbox.social_network_authorization.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_authorization.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByAccountId(UUID accountId);
}
