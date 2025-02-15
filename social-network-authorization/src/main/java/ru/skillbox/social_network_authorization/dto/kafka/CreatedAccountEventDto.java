package ru.skillbox.social_network_authorization.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedAccountEventDto {

    private UUID userId;

    private UUID accountId;

}

