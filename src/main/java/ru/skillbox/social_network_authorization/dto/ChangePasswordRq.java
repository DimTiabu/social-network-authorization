package ru.skillbox.social_network_authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordRq {
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
}