package com.civic.complaint.controller;

import com.civic.complaint.dto.request.AuthRequest;
import com.civic.complaint.dto.response.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register - should return 201 with token")
    void register_Returns201() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("Password@1");
        request.setFullName("New User");

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(1L).username("newuser").email("new@test.com")
                .fullName("New User").createdAt(LocalDateTime.now()).build();

        AuthResponse.JwtToken token = AuthResponse.JwtToken.builder()
                .accessToken("mock-token").tokenType("Bearer")
                .expiresIn(86400000L).user(userInfo).build();

        when(authService.register(any())).thenReturn(token);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-token"));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 on invalid input")
    void register_InvalidInput_Returns400() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setUsername("a"); // too short
        request.setEmail("not-an-email");
        request.setPassword("123");
        request.setFullName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 200 with token")
    void login_Returns200() throws Exception {
        AuthRequest.Login request = new AuthRequest.Login();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password");

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(1L).username("testuser").build();
        AuthResponse.JwtToken token = AuthResponse.JwtToken.builder()
                .accessToken("mock-token").tokenType("Bearer")
                .expiresIn(86400000L).user(userInfo).build();

        when(authService.login(any())).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("mock-token"));
    }
}
