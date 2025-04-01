package ru.skillbox.social_network_authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.social_network_authorization.configuration.TestSecurityConfiguration;
import ru.skillbox.social_network_authorization.dto.*;
import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.exception.EntityNotFoundException;
import ru.skillbox.social_network_authorization.exception.InvalidPasswordException;
import ru.skillbox.social_network_authorization.security.AppUserDetails;
import ru.skillbox.social_network_authorization.service.AuthService;
import ru.skillbox.social_network_authorization.service.JwtService;
import ru.skillbox.social_network_authorization.service.impl.RefreshTokenService;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void givenValidCredentials_whenLogin_thenReturnTokenResponse() throws Exception {
        AuthenticateRq request = new AuthenticateRq("test@email.com", "password");
        TokenResponse response = new TokenResponse("accessToken123", "refreshToken456");

        when(authService.authenticate(Mockito.any(), Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken456"));
    }

    @Test
    void givenInvalidPassword_whenLogin_thenReturnBadRequest() throws Exception {
        AuthenticateRq request = new AuthenticateRq("test@email.com", "wrong_password");

        when(authService.authenticate(Mockito.any(), Mockito.any())).thenThrow(new InvalidPasswordException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidEmail_whenLogin_thenReturnNotFound() throws Exception {
        AuthenticateRq request = new AuthenticateRq("wrong@email.com", "password");

        when(authService.authenticate(Mockito.any(), Mockito.any()))
                .thenThrow(new EntityNotFoundException("Пользователь не зарегистрирован"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Пользователь не зарегистрирован"));
    }

    @Test
    void givenValidToken_whenValidateToken_thenReturnTrue() throws Exception {
        String token = "valid-token";
        when(jwtService.validate(token)).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/validate").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void givenValidRefreshToken_whenRefreshToken_thenReturnNewTokens() throws Exception {
        String refreshToken = "valid-refresh-token";
        AppUserDetails userDetails = new AppUserDetails(User.builder().build());
        TokenResponse response = new TokenResponse("newAccessToken", "newRefreshToken");

        when(refreshTokenService.getUserByRefreshToken(refreshToken)).thenReturn(userDetails);
        when(refreshTokenService.refreshTokens(refreshToken, userDetails)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void givenValidRequest_whenLogout_thenReturnSuccessMessage() throws Exception {
        when(refreshTokenService.logout()).thenReturn("Успешный выход из аккаунта");

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Успешный выход из аккаунта"));
    }

    @Test
    void givenValidEmail_whenRequestChangeEmail_thenReturnSuccessMessage() throws Exception {
        Map<String, Map<String, String>> payload = Map.of("email", Map.of("email", "newemail@example.com"));
        String token = "valid-jwt-token";

        when(jwtService.getUsername(token)).thenReturn("test@example.com");
        when(authService.changeEmail("newemail@example.com", "test@example.com")).thenReturn("Ссылка для смены email отправлена");

        mockMvc.perform(post("/api/v1/auth/change-email-link")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Ссылка для смены email отправлена"));
    }

    @Test
    void givenValidPasswordChangeRequest_whenRequestChangePassword_thenReturnSuccessMessage() throws Exception {
        Map<String, String> payload = Map.of(
                "oldPassword", "oldPassword123",
                "newPassword1", "newPassword123",
                "newPassword2", "newPassword123"
        );
        String token = "valid-jwt-token";

        when(jwtService.getUsername(token)).thenReturn("test@example.com");
        when(authService.changePassword(Mockito.any(), Mockito.any())).thenReturn("Пароль успешно изменён");

        mockMvc.perform(post("/api/v1/auth/change-password-link")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пароль успешно изменён"));
    }

    @Test
    void givenNoToken_whenRequestChangeEmail_thenReturnUnauthorized() throws Exception {
        Map<String, Map<String, String>> payload = Map.of("email", Map.of("email", "newemail@example.com"));

        mockMvc.perform(post("/api/v1/auth/change-email-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNoToken_whenRequestChangePassword_thenReturnUnauthorized() throws Exception {
        Map<String, String> payload = Map.of(
                "oldPassword", "oldPassword123",
                "newPassword1", "newPassword123",
                "newPassword2", "newPassword123"
        );

        mockMvc.perform(post("/api/v1/auth/change-password-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
}