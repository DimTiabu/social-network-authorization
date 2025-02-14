package ru.skillbox.social_network_authorization.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecoveryPasswordLinkRq {
    @NotBlank(message = "Заполните поле \"temp\"")
    private String temp;

    @NotBlank(message = "Укажите электронную почту")
    @Email(message = "Неправильный формат электронной почты")
    private String email;

}
