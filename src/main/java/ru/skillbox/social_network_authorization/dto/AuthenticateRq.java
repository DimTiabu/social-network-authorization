package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateRq {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}