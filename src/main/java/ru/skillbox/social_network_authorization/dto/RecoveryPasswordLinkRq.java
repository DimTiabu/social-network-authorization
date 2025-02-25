package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RecoveryPasswordLinkRq {

    private String temp = UUID.randomUUID().toString();

    @NotBlank(message = "Укажите электронную почту")
    @Email(message = "Неправильный формат электронной почты")
    private String email;

}
