package ru.skillbox.social_network_authorization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeEmailRequest {
    private String email;
    private String refreshToken;
}
