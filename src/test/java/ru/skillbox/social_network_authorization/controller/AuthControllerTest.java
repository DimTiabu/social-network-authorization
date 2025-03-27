package ru.skillbox.social_network_authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.social_network_authorization.configuration.TestSecurityConfiguration;
import ru.skillbox.social_network_authorization.dto.AuthenticateRq;
import ru.skillbox.social_network_authorization.dto.TokenResponse;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.service.JwtService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testLogin_Success() throws Exception {
        // Подготовка данных
        AuthenticateRq request = new AuthenticateRq("test@email.com", "password");
        TokenResponse response = new TokenResponse("accessToken123", "refreshToken456");

        when(authService.authenticate(Mockito.any(), Mockito.any())).thenReturn(response);

        // Отправка запроса и проверка ответа
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken456"));
    }

    @Test
    void testLogin_Fail_InvalidPassword() throws Exception {
        AuthenticateRq request = new AuthenticateRq("test@email.com", "wrong_password");

        when(authService.authenticate(Mockito.any(), Mockito.any())).thenThrow(new InvalidPasswordException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Fail_InvalidEmail() throws Exception {
        AuthenticateRq request = new AuthenticateRq("wrong@email.com", "password");

        when(authService.authenticate(Mockito.any(), Mockito.any()))
                .thenThrow(new EntityNotFoundException("Пользователь не зарегистрирован"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Пользователь не зарегистрирован"));
    }
}
