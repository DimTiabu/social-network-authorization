package ru.tyabutov.social_network_authorization.controller;

import org.springframework.web.bind.annotation.ResponseStatus;
import ru.tyabutov.social_network_authorization.exception.*;
import ru.tyabutov.social_network_authorization.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@ResponseStatus(HttpStatus.BAD_REQUEST)
@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(
            EntityNotFoundException ex) {
        log.error("Ошибка при попытке получить сущность", ex);
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public ErrorResponse passwordsDoNotMatch(PasswordsDoNotMatchException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ErrorResponse invalidPassword(InvalidPasswordException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ErrorResponse invalidRefreshToken(RefreshTokenException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ExceptionHandler(CaptchaException.class)
    public ErrorResponse invalidCaptcha(CaptchaException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse noAccess(
            SecurityException ex) {
        log.error("Ошибка! Недостаточно прав!", ex);
        return new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse illegalArgument(IllegalArgumentException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwtAuthenticationException(JwtAuthenticationException ex) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse anyException(Exception ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage());
    }

}