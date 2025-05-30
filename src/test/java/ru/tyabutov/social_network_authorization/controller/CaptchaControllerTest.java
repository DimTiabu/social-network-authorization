package ru.tyabutov.social_network_authorization.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.tyabutov.social_network_authorization.configuration.TestSecurityConfiguration;
import ru.tyabutov.social_network_authorization.dto.CaptchaDto;
import ru.tyabutov.social_network_authorization.service.CaptchaService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaptchaController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfiguration.class)
class CaptchaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaptchaService captchaService;

    @Test
    void generateCaptcha_ShouldReturnCaptchaDto() throws Exception {
        // Arrange
        CaptchaDto captchaDto = new CaptchaDto("secret123", "imageData");
        when(captchaService.generateCaptcha()).thenReturn(captchaDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret").value("secret123"))
                .andExpect(jsonPath("$.image").value("imageData"));
    }
}