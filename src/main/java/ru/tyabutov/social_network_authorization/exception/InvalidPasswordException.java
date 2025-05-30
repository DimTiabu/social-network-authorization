package ru.tyabutov.social_network_authorization.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Неверный пароль. Проверьте введённые данные.");
    }
}