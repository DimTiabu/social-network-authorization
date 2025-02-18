package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RecoveryPasswordLinkRq {

    private String temp = "12333333";

    @NotBlank(message = "Укажите электронную почту")
    @Email(message = "Неправильный формат электронной почты")
    private String email;

}
