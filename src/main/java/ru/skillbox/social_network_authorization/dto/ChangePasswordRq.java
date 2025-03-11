package ru.skillbox.social_network_authorization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRq {
    private String oldPassword;
    private String newPassword;
}