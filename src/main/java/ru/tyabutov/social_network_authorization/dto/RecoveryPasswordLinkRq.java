package ru.tyabutov.social_network_authorization.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RecoveryPasswordLinkRq {

    private String temp = UUID.randomUUID().toString().substring(0, 8);

    @NotBlank(message = "Укажите электронную почту")
    @Email(message = "Неправильный формат электронной почты")
    private String email;

    @JsonCreator
    public RecoveryPasswordLinkRq(String email) {
        this.email = email;
    }
}