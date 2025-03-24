package ru.skillbox.social_network_authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.skillbox.social_network_authorization.configuration.TestSecurityConfiguration;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import ru.skillbox.social_network_authorization.dto.kafka.RegistrationEventDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import ru.skillbox.social_network_authorization.service.impl.KafkaMessageService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private KafkaMessageService kafkaMessageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_Success() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "test@example.com", "password", "password", "John", "Doe", "1234"
        );
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();

        when(registrationService.registerUser(any(User.class))).thenReturn(user);
        doNothing().when(kafkaMessageService).sendMessageWithUserData(any(RegistrationEventDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto))
                        .sessionAttr("captchaSecret", "1234"))  // Устанавливаем атрибут сессии
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Успешная регистрация"));

        verify(registrationService).registerUser(any(User.class));
        verify(kafkaMessageService).sendMessageWithUserData(any(RegistrationEventDto.class));
    }

    @Test
    void register_InvalidCaptcha_ThrowsException() throws Exception {
        RegistrationDto registrationDto = RegistrationDto.builder()
                .email("test@example.com")
                .password1("password")
                .password2("password")
                .firstName("John")
                .lastName("Doe")
                .captchaCode("wrongCaptcha")
                .build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto))
                        .sessionAttr("captchaSecret", "1234"))  // Устанавливаем правильную капчу, но в запросе неверная
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(registrationService, never()).registerUser(any(User.class));
        verify(kafkaMessageService, never()).sendMessageWithUserData(any(RegistrationEventDto.class));
    }
}