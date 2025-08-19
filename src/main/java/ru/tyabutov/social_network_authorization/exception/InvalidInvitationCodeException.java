package ru.tyabutov.social_network_authorization.exception;

public class InvalidInvitationCodeException extends RuntimeException {
    public InvalidInvitationCodeException(String message) {
        super(message);
    }
}
