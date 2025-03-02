package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.skillbox.social_network_authorization.entity.RefreshToken;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticateResponse {
    private String accessToken;
    private String refreshToken;
}
