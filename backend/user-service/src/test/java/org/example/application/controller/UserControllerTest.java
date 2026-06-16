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

  // ===================== REGISTER =====================

  @Test
  void register_validRequest_shouldReturn200() throws Exception {
    when(userService.register(any())).thenReturn(authResponse);

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "john@example.com",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Registration successful"))
      .andExpect(jsonPath("$.user.email").value("john@example.com"));
  }

  @Test
  void register_missingEmail_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void register_missingPassword_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "john@example.com",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void register_missingFullName_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "email", "john@example.com",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void register_invalidEmail_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "not-an-email",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void register_duplicateEmail_shouldReturn400() throws Exception {
    when(userService.register(any()))
      .thenThrow(new RuntimeException("User with this email already exists"));

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "John Doe",
      "email", "john@example.com",
      "password", "password123",
      "role", "RENTER"
    ));

    mockMvc.perform(post("/user/v1/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  // ===================== LOGIN =====================

  @Test
  void login_validCredentials_shouldReturn200() throws Exception {
    AuthResponse loginResponse = AuthResponse.builder()
      .message("Login successful")
      .user(userResponse)
      .build();

    when(userService.login(any())).thenReturn(loginResponse);

    String request = objectMapper.writeValueAsString(Map.of(
      "email", "john@example.com",
      "password", "password123"
    ));

    mockMvc.perform(post("/user/v1/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Login successful"))
      .andExpect(jsonPath("$.user.email").value("john@example.com"));
  }

  @Test
  void login_invalidCredentials_shouldReturn401InBody() throws Exception {
    when(userService.login(any()))
      .thenThrow(new InvalidCredentialsException("Invalid email or password"));

    String request = objectMapper.writeValueAsString(Map.of(
      "email", "john@example.com",
      "password", "wrongpassword"
    ));

    mockMvc.perform(post("/user/v1/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(401))
      .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void login_missingEmail_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "password", "password123"
    ));

    mockMvc.perform(post("/user/v1/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void login_missingPassword_shouldReturn400() throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
      "email", "john@example.com"
    ));

    mockMvc.perform(post("/user/v1/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  @Test
  void login_deactivatedAccount_shouldReturn400() throws Exception {
    when(userService.login(any()))
      .thenThrow(new RuntimeException("Account is deactivated"));

    String request = objectMapper.writeValueAsString(Map.of(
      "email", "john@example.com",
      "password", "password123"
    ));

    mockMvc.perform(post("/user/v1/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  // ===================== CREATE USER =====================

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

  // ===================== GET ALL USERS =====================

  @Test
  void getAllUsers_shouldReturn200WithList() throws Exception {
    when(userService.getAllUsers()).thenReturn(List.of(userResponse));

    mockMvc.perform(get("/user/v1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].email").value("john@example.com"));
  }

  @Test
  void getAllUsers_emptyList_shouldReturn200WithEmptyArray() throws Exception {
    when(userService.getAllUsers()).thenReturn(List.of());

    mockMvc.perform(get("/user/v1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }

  // ===================== GET USER BY ID =====================

  @Test
  void getUserById_exists_shouldReturn200() throws Exception {
    when(userService.getUserById(1L)).thenReturn(userResponse);

    mockMvc.perform(get("/user/v1/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.fullName").value("John Doe"));
  }

  @Test
  void getUserById_notFound_shouldReturn400() throws Exception {
    when(userService.getUserById(999L))
      .thenThrow(new RuntimeException("User not found"));

    mockMvc.perform(get("/user/v1/999"))
      .andExpect(status().isBadRequest());
  }

  // ===================== UPDATE USER =====================

  @Test
  void updateUser_validRequest_shouldReturn200() throws Exception {
    UserResponse updated = UserResponse.builder()
      .id(1L)
      .fullName("Updated Name")
      .email("updated@example.com")
      .role(UserRole.OWNER)
      .isActive(true)
      .createdAt(LocalDate.now())
      .build();

    when(userService.updateUser(eq(1L), any())).thenReturn(updated);

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "Updated Name",
      "email", "updated@example.com",
      "password", "newpassword",
      "role", "OWNER"
    ));

    mockMvc.perform(put("/user/v1/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.fullName").value("Updated Name"))
      .andExpect(jsonPath("$.role").value("OWNER"));
  }

  @Test
  void updateUser_notFound_shouldReturn400() throws Exception {
    when(userService.updateUser(eq(999L), any()))
      .thenThrow(new RuntimeException("User not found"));

    String request = objectMapper.writeValueAsString(Map.of(
      "fullName", "Name",
      "email", "email@example.com",
      "password", "pass123",
      "role", "RENTER"
    ));

    mockMvc.perform(put("/user/v1/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());
  }

  // ===================== DELETE USER =====================

  @Test
  void deleteUser_shouldReturn204() throws Exception {
    doNothing().when(userService).deleteUser(1L);

    mockMvc.perform(delete("/user/v1/1"))
      .andExpect(status().isNoContent());
  }

  @Test
  void deleteUser_notFound_shouldReturn400() throws Exception {
    doThrow(new RuntimeException("User not found"))
      .when(userService).deleteUser(999L);

    mockMvc.perform(delete("/user/v1/999"))
      .andExpect(status().isBadRequest());
  }

  // ===================== ACTIVATE USER =====================

  @Test
  void activateUser_shouldReturn200() throws Exception {
    UserResponse activated = UserResponse.builder()
      .id(1L)
      .fullName("John Doe")
      .email("john@example.com")
      .role(UserRole.RENTER)
      .isActive(true)
      .createdAt(LocalDate.now())
      .build();

    when(userService.activateUser(1L)).thenReturn(activated);

    mockMvc.perform(patch("/user/v1/1/activate"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isActive").value(true));
  }

  @Test
  void activateUser_notFound_shouldReturn400() throws Exception {
    when(userService.activateUser(999L))
      .thenThrow(new RuntimeException("User not found"));

    mockMvc.perform(patch("/user/v1/999/activate"))
      .andExpect(status().isBadRequest());
  }

  // ===================== DEACTIVATE USER =====================

  @Test
  void deactivateUser_shouldReturn200() throws Exception {
    UserResponse deactivated = UserResponse.builder()
      .id(1L)
      .fullName("John Doe")
      .email("john@example.com")
      .role(UserRole.RENTER)
      .isActive(false)
      .createdAt(LocalDate.now())
      .build();

    when(userService.deactivateUser(1L)).thenReturn(deactivated);

    mockMvc.perform(patch("/user/v1/1/deactivate"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isActive").value(false));
  }

  @Test
  void deactivateUser_notFound_shouldReturn400() throws Exception {
    when(userService.deactivateUser(999L))
      .thenThrow(new RuntimeException("User not found"));

    mockMvc.perform(patch("/user/v1/999/deactivate"))
      .andExpect(status().isBadRequest());
  }
}
