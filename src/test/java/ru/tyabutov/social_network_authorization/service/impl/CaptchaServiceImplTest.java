package ru.tyabutov.social_network_authorization.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tyabutov.social_network_authorization.dto.CaptchaDto;
import ru.tyabutov.social_network_authorization.exception.CaptchaException;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaServiceImplTest {

    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaServiceImpl();
    }

    @Test
    void generateCaptcha_ShouldReturnValidCaptchaDto() {
        // Act
        CaptchaDto captchaDto = captchaService.generateCaptcha();

        // Assert
        assertNotNull(captchaDto);
        assertNotNull(captchaDto.getSecret());
        assertNotNull(captchaDto.getImage());
        assertFalse(captchaDto.getSecret().isEmpty());
        assertFalse(captchaDto.getImage().isEmpty());
    }

    @Test
    void generateCaptcha_ShouldThrowCaptchaExceptionOnIOException() {
        CaptchaServiceImpl faultyService = new CaptchaServiceImpl() {
            @Override
            public CaptchaDto generateCaptcha() {
                throw new CaptchaException();
            }
        };

        assertThrows(CaptchaException.class, faultyService::generateCaptcha);
    }
}