package ru.tyabutov.social_network_authorization.exception;

public class CaptchaException extends RuntimeException {
  public CaptchaException() {
    super("Ошибка генерации капчи");
  }

  public CaptchaException(String message) {
    super(message);
  }
}
