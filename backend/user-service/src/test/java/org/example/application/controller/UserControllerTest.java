package org.example.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.application.UserApplicationService;
import org.example.controller.UserController;
import org.example.domain.UserRole;
import org.example.dto.AuthResponse;
import org.example.dto.UserResponse;
import org.example.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(org.example.config.SecurityConfig.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserApplicationService userService;

  private ObjectMapper objectMapper;
  private UserResponse userResponse;
  private AuthResponse authResponse;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    userResponse = UserResponse.builder()
      .id(1L)
      .fullName("John Doe")
      .email("john@example.com")
      .role(UserRole.RENTER)
      .isActive(true)
      .createdAt(LocalDate.now())
      .build();

    authResponse = AuthResponse.builder()
      .message("Registration successful")
      .user(userResponse)
      .build();
  }

  @Test
  void createUser_validRequest_shouldReturn200() throws Exception {
    when(userService.createUser(any())).thenReturn(userResponse);

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "john@example.com",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("john@example.com"))
      .andExpect(jsonPath("$.role").value("RENTER"));
  }

  @Test
  void createUser_duplicateEmail_shouldReturn400() throws Exception {
    when(userService.createUser(any()))
      .thenThrow(new RuntimeException("User with this email already exists"));

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "john@example.com",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }
}
