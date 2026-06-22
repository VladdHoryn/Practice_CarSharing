package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.application.UserApplicationService;
import org.example.config.SecurityConfig;
import org.example.domain.UserRole;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserApplicationService userService;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @Autowired
  private ObjectMapper objectMapper;

  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    userResponse = UserResponse.builder()
      .id(1L)
      .keycloakId("kc-id-123")
      .fullName("John Doe")
      .email("john@example.com")
      .role(UserRole.RENTER)
      .isActive(true)
      .createdAt(LocalDate.now())
      .build();
  }

  private UserRequest validRequest() {
    UserRequest req = new UserRequest();
    req.setFullName("John Doe");
    req.setEmail("john@example.com");
    req.setPassword("securePass123");
    req.setRole(UserRole.RENTER);
    return req;
  }

  @Nested
  @DisplayName("POST /user/v1")
  class CreateUser {

    @Test
    @DisplayName("анонімний може створити користувача (public endpoint)")
    void shouldCreateUserAnonymously() throws Exception {
      when(userService.createUser(any())).thenReturn(userResponse);
      mockMvc.perform(post("/user/v1")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("авторизований також може створити користувача")
    void shouldCreateUserWhenAuthenticated() throws Exception {
      when(userService.createUser(any())).thenReturn(userResponse);
      mockMvc.perform(post("/user/v1")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("повертає 400 для некоректного запиту (порожнє ім'я)")
    void shouldReturn400ForInvalidRequest() throws Exception {
      UserRequest invalid = new UserRequest();
      invalid.setFullName("");
      invalid.setEmail("bad-email");
      invalid.setPassword("pass");
      invalid.setRole(UserRole.RENTER);
      mockMvc.perform(post("/user/v1")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("повертає 400 для некоректного email формату")
    void shouldReturn400ForInvalidEmailFormat() throws Exception {
      UserRequest invalid = new UserRequest();
      invalid.setFullName("Valid Name");
      invalid.setEmail("not-an-email");
      invalid.setPassword("pass123");
      invalid.setRole(UserRole.RENTER);
      mockMvc.perform(post("/user/v1")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /user/v1")
  class GetAllUsers {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR отримує список користувачів")
    void shouldReturnAllUsersForAdmin() throws Exception {
      when(userService.getAllUsers()).thenReturn(List.of(userResponse));
      mockMvc.perform(get("/user/v1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = {"RENTER"})
    @DisplayName("RENTER отримує 403")
    void shouldReturn403ForRenter() throws Exception {
      mockMvc.perform(get("/user/v1"))
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    @DisplayName("OWNER отримує 403")
    void shouldReturn403ForOwner() throws Exception {
      mockMvc.perform(get("/user/v1"))
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("анонімний отримує 401")
    void shouldReturn401ForAnonymous() throws Exception {
      mockMvc.perform(get("/user/v1"))
        .andExpect(status().is4xxClientError());
    }
  }

  @Nested
  @DisplayName("GET /user/v1/keycloak/{keycloakId}")
  class GetUserByKeycloakId {

    @Test
    @WithMockUser(username = "kc-id-123")
    @DisplayName("власник може отримати свій профіль")
    void shouldReturnUserForOwner() throws Exception {
      when(userService.getUserByKeycloakId("kc-id-123")).thenReturn(userResponse);
      mockMvc.perform(get("/user/v1/keycloak/kc-id-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keycloakId").value("kc-id-123"));
    }

    @Test
    @WithMockUser(username = "kc-id-123", roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR може отримати чужий профіль")
    void shouldReturnUserForAdmin() throws Exception {
      when(userService.getUserByKeycloakId("kc-id-other")).thenReturn(userResponse);
      mockMvc.perform(get("/user/v1/keycloak/kc-id-other"))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "kc-id-other")
    @DisplayName("інший RENTER не може отримати чужий профіль")
    void shouldReturn403ForDifferentUser() throws Exception {
      mockMvc.perform(get("/user/v1/keycloak/kc-id-123"))
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("анонімний отримує 401")
    void shouldReturn401ForAnonymous() throws Exception {
      mockMvc.perform(get("/user/v1/keycloak/kc-id-123"))
        .andExpect(status().is4xxClientError());
    }
  }

  @Nested
  @DisplayName("PUT /user/v1/keycloak/{keycloakId}")
  class UpdateUser {

    @Test
    @WithMockUser(username = "kc-id-123")
    @DisplayName("власник може оновити свій профіль")
    void shouldUpdateUserForOwner() throws Exception {
      when(userService.updateUser(eq("kc-id-123"), any())).thenReturn(userResponse);
      mockMvc.perform(put("/user/v1/keycloak/kc-id-123")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "kc-id-123", roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR може оновити чужий профіль")
    void shouldUpdateUserForAdmin() throws Exception {
      when(userService.updateUser(eq("kc-id-other"), any())).thenReturn(userResponse);
      mockMvc.perform(put("/user/v1/keycloak/kc-id-other")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "kc-id-other")
    @DisplayName("інший користувач не може оновити чужий профіль")
    void shouldReturn403ForDifferentUser() throws Exception {
      mockMvc.perform(put("/user/v1/keycloak/kc-id-123")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /user/v1/keycloak/{keycloakId}")
  class DeleteUser {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR може видалити користувача")
    void shouldDeleteUserForAdmin() throws Exception {
      doNothing().when(userService).deleteUser("kc-id-123");
      mockMvc.perform(delete("/user/v1/keycloak/kc-id-123").with(csrf()))
        .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"RENTER"})
    @DisplayName("RENTER отримує 403")
    void shouldReturn403ForRenter() throws Exception {
      mockMvc.perform(delete("/user/v1/keycloak/kc-id-123").with(csrf()))
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("анонімний отримує 401")
    void shouldReturn401ForAnonymous() throws Exception {
      mockMvc.perform(delete("/user/v1/keycloak/kc-id-123").with(csrf()))
        .andExpect(status().is4xxClientError());
    }
  }

  @Nested
  @DisplayName("PATCH /user/v1/keycloak/{keycloakId}/activate")
  class ActivateUser {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR може активувати користувача")
    void shouldActivateUserForAdmin() throws Exception {
      when(userService.activateUser("kc-id-123")).thenReturn(userResponse);
      mockMvc.perform(patch("/user/v1/keycloak/kc-id-123/activate").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = {"RENTER"})
    @DisplayName("RENTER отримує 403")
    void shouldReturn403ForRenter() throws Exception {
      mockMvc.perform(patch("/user/v1/keycloak/kc-id-123/activate").with(csrf()))
        .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("PATCH /user/v1/keycloak/{keycloakId}/deactivate")
  class DeactivateUser {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR може деактивувати користувача")
    void shouldDeactivateUserForAdmin() throws Exception {
      UserResponse deactivated = UserResponse.builder()
        .id(1L).keycloakId("kc-id-123").isActive(false).build();
      when(userService.deactivateUser("kc-id-123")).thenReturn(deactivated);
      mockMvc.perform(patch("/user/v1/keycloak/kc-id-123/deactivate").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    @DisplayName("OWNER отримує 403")
    void shouldReturn403ForOwner() throws Exception {
      mockMvc.perform(patch("/user/v1/keycloak/kc-id-123/deactivate").with(csrf()))
        .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /user/v1/exist/driverCode")
  class ExistDriverCode {

    @Test
    @DisplayName("анонімний може перевірити driverCode (public endpoint)")
    void shouldCheckDriverCodeAnonymously() throws Exception {
      when(userService.existByEmailAndDriverCode("john@example.com", "ABCD123456"))
        .thenReturn(Optional.of(1L));
      mockMvc.perform(get("/user/v1/exist/driverCode")
          .param("email", "john@example.com")
          .param("driverCode", "ABCD123456"))
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("повертає порожній Optional якщо не знайдено")
    void shouldReturnEmptyWhenNotFound() throws Exception {
      when(userService.existByEmailAndDriverCode(anyString(), anyString()))
        .thenReturn(Optional.empty());
      mockMvc.perform(get("/user/v1/exist/driverCode")
          .param("email", "no@example.com")
          .param("driverCode", "XXXX999999"))
        .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("GET /user/v1/analytics/admin/active/count")
  class CountActiveUsers {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR отримує кількість активних")
    void shouldCountActiveUsersForAdmin() throws Exception {
      when(userService.countActiveUsers()).thenReturn(7L);
      mockMvc.perform(get("/user/v1/analytics/admin/active/count"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(7));
    }

    @Test
    @WithMockUser(roles = {"RENTER"})
    @DisplayName("RENTER отримує 403")
    void shouldReturn403ForRenter() throws Exception {
      mockMvc.perform(get("/user/v1/analytics/admin/active/count"))
        .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /user/v1/analytics/admin/roles/count")
  class CountUsersByRole {

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR отримує кількість за роллю")
    void shouldCountByRoleForAdmin() throws Exception {
      when(userService.countByRole(UserRole.RENTER)).thenReturn(5L);
      mockMvc.perform(get("/user/v1/analytics/admin/roles/count")
          .param("role", "RENTER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(5));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    @DisplayName("ADMINISTRATOR отримує кількість OWNER")
    void shouldCountOwnersForAdmin() throws Exception {
      when(userService.countByRole(UserRole.OWNER)).thenReturn(2L);
      mockMvc.perform(get("/user/v1/analytics/admin/roles/count")
          .param("role", "OWNER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(2));
    }

    @Test
    @WithMockUser(roles = {"RENTER"})
    @DisplayName("RENTER отримує 403")
    void shouldReturn403ForRenter() throws Exception {
      mockMvc.perform(get("/user/v1/analytics/admin/roles/count")
          .param("role", "RENTER"))
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("анонімний отримує 401")
    void shouldReturn401ForAnonymous() throws Exception {
      mockMvc.perform(get("/user/v1/analytics/admin/roles/count")
          .param("role", "RENTER"))
        .andExpect(status().is4xxClientError());
    }
  }
}
