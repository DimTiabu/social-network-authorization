package ru.tyabutov.social_network_authorization.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaDto {
    private String secret;
    private String image;
}