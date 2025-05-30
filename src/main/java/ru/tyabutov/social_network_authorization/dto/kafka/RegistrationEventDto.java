package ru.tyabutov.social_network_authorization.dto.kafka;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationEventDto {

    private UUID userId;

    private String email;

    private String firstName;

    private String lastName;

}