package com.example.social_network_authorization.web.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordRq {
    @NotBlank(message = "Заполните поле \"temp\"")
    private String temp;

    @NotBlank
    private String password;

}
