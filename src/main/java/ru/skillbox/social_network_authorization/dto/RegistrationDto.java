package ru.skillbox.social_network_authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {
    @NotEmpty(message = "Укажите электронную почту")
    @Email(message = "Неправильный формат электронной почты")
    private String email;

    @NotEmpty(message = "Пароль1 не может быть пустым")
    private String password1;

    @NotEmpty(message = "Пароль2 не может быть пустым")
    private String password2;

    @NotEmpty(message = "Заполните поле \"Имя\"")
    private String firstName;

    @NotEmpty(message = "Заполните поле \"Фамилия\"")
    private String lastName;

    private String captchaCode;

    @Builder.Default
    private String token = null;

}
