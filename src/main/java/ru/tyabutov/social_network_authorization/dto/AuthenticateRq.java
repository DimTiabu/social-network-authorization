package ru.tyabutov.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticateRq {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}