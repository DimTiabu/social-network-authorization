package ru.skillbox.social_network_authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.service.RegistrationService;
import ru.skillbox.social_network_authorization.service.impl.KafkaMessageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Profile("test")
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private KafkaMessageService kafkaMessageService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private RegistrationController registrationController;

    private MockMvc mockMvc;

    @Test
    void testSuccessfulRegistration() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();

        RegistrationDto registrationDto = RegistrationDto.builder()
                .email("test@example.com")
                .password1("password123")
                .password2("password123")
                .firstName("John")
                .lastName("Doe")
                .captchaCode("captchaCode")
                .build();

        when(session.getAttribute("captchaSecret")).thenReturn("captchaCode");
        when(registrationService.registerUser(any())).thenReturn(User.builder().build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("captchaSecret", "captchaCode")
                        .content(asJsonString(registrationDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidCaptcha() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();

        RegistrationDto registrationDto = RegistrationDto.builder()
                .email("test@example.com")
                .password1("password123")
                .password2("password123")
                .firstName("John")
                .lastName("Doe")
                .captchaCode("invalidCaptcha")
                .build();

        when(session.getAttribute("captchaSecret")).thenReturn("captchaCode");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("captchaSecret", "captchaCode")
                        .content(asJsonString(registrationDto)))
                .andExpect(status().is5xxServerError());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}