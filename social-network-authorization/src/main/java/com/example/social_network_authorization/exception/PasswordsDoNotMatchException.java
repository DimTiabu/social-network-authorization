package com.example.social_network_authorization.exception;

public class PasswordsDoNotMatchException extends RuntimeException {
    public PasswordsDoNotMatchException() {
        super("Пароли не совпадают");
    }
}