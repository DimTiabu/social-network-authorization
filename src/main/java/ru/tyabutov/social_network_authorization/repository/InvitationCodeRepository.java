package ru.tyabutov.social_network_authorization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tyabutov.social_network_authorization.entity.InvitationCode;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, UUID> {

    Optional<InvitationCode> findByEmailAndConfirmationCode(String email, String confirmationCode);

}