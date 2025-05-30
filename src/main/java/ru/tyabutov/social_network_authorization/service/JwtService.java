package ru.tyabutov.social_network_authorization.service;

import ru.tyabutov.social_network_authorization.security.AppUserDetails;

public interface JwtService {

    String generateJwtToken(AppUserDetails userDetails);

    String getUsername(String token);

    boolean validate(String authToken);
}