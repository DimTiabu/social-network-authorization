package ru.tyabutov.social_network_authorization.exception;

public class MappingException extends RuntimeException {
  public MappingException() {
    super("Ошибка маппинга");
  }
}
