package ru.skillbox.social_network_authorization.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaDto {
    private String token;
    private String image;
}