package ru.skillbox.social_network_authorization.exception;

public class ExpiredJwtException extends RuntimeException {
  public ExpiredJwtException() {
    super("Токен просрочен");
  }
}
