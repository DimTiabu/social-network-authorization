package ru.tyabutov.social_network_authorization.dto.kafka;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatedAccountEventDto {

    private UUID userId;

    private UUID accountId;

}

