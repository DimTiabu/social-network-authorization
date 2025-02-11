package com.example.social_network_authorization.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticateRq {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}
