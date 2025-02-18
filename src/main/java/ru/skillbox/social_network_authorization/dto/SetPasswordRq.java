package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordRq {
    @NotBlank(message = "Введите код восстановления")
    private String temp;

    @NotBlank(message = "Введите новый пароль")
    private String password;

}
