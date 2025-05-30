package ru.tyabutov.social_network_authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordRq {
    private String newPassword1;
    private String newPassword2;
    private String oldPassword;
}