package ru.skillbox.social_network_authorization.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationEventDto {

    private String email;

    private String firstName;

    private String lastName;

}


