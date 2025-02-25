package ru.skillbox.social_network_authorization.dto.kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationEventDto {

    private String email;

    private String firstName;

    private String lastName;

}


