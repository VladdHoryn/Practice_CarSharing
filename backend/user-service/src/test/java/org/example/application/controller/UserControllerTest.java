package org.example.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.example.application.UserApplicationService;
import org.example.config.SecurityConfig;
import org.example.controller.UserController;
import org.example.domain.UserRole;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserApplicationService userApplicationService;

    @MockitoBean private JwtDecoder jwtDecoder;

    private UserResponse sampleUserResponse() {

        return UserResponse.builder()
                .id(1L)
                .keycloakId("kc-id-123")
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.RENTER)
                .isActive(true)
                .createdAt(java.time.LocalDate.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /user/v1")
    class GetAllUsersTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.getAllUsers()).thenReturn(List.of(sampleUserResponse()));

            mockMvc.perform(get("/user/v1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].keycloakId").value("kc-id-123"));
        }

        @Test
        @DisplayName("403 для RENTER")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(get("/user/v1")).andExpect(status().isForbidden());
            verify(userApplicationService, never()).getAllUsers();
        }

        @Test
        @DisplayName("401 для неавторизованого запиту")
        void shouldReturnUnauthorized_WhenAnonymous() throws Exception {
            mockMvc.perform(get("/user/v1")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /user/v1")
    class CreateUserTests {

        @Test
        @DisplayName("200 для будь-кого без автентифікації (permitAll)")
        void shouldReturnOk_WhenAnonymous() throws Exception {
            UserRequest request = new UserRequest();
            request.setEmail("new@example.com");
            request.setPassword("pass123");
            request.setFullName("New User");
            request.setRole(UserRole.RENTER);

            when(userApplicationService.createUser(any())).thenReturn(sampleUserResponse());

            mockMvc.perform(
                            post("/user/v1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 при невалідному тілі запиту")
        void shouldReturnBadRequest_WhenInvalidBody() throws Exception {
            UserRequest request = new UserRequest();

            mockMvc.perform(
                            post("/user/v1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /user/v1/keycloak/{keycloakId}")
    class GetUserByKeycloakIdTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(
                username = "someone-else",
                roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.getUserByKeycloakId("kc-id-123"))
                    .thenReturn(sampleUserResponse());

            mockMvc.perform(get("/user/v1/keycloak/{keycloakId}", "kc-id-123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("200 коли власник запитує свій профіль")
        @WithMockUser(
                username = "kc-id-123",
                roles = {"RENTER"})
        void shouldReturnOk_WhenOwner() throws Exception {
            when(userApplicationService.getUserByKeycloakId("kc-id-123"))
                    .thenReturn(sampleUserResponse());

            mockMvc.perform(get("/user/v1/keycloak/{keycloakId}", "kc-id-123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("403 коли RENTER запитує чужий профіль")
        @WithMockUser(
                username = "another-user",
                roles = {"RENTER"})
        void shouldReturnForbidden_WhenNotOwnerAndNotAdmin() throws Exception {
            mockMvc.perform(get("/user/v1/keycloak/{keycloakId}", "kc-id-123"))
                    .andExpect(status().isForbidden());
            verify(userApplicationService, never()).getUserByKeycloakId(anyString());
        }
    }

    @Nested
    @DisplayName("PUT /user/v1/keycloak/{keycloakId}")
    class UpdateUserTests {

        @Test
        @DisplayName("200 коли власник оновлює свій профіль")
        @WithMockUser(
                username = "kc-id-123",
                roles = {"RENTER"})
        void shouldReturnOk_WhenOwner() throws Exception {
            UserRequest request = new UserRequest();
            request.setFullName("Updated Name");
            request.setEmail("updated@example.com");

            when(userApplicationService.updateUser(eq("kc-id-123"), any()))
                    .thenReturn(sampleUserResponse());

            mockMvc.perform(
                            put("/user/v1/keycloak/{keycloakId}", "kc-id-123")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("403 коли RENTER оновлює чужий профіль")
        @WithMockUser(
                username = "another-user",
                roles = {"RENTER"})
        void shouldReturnForbidden_WhenNotOwnerAndNotAdmin() throws Exception {
            UserRequest request = new UserRequest();
            request.setFullName("Hacked Name");
            request.setEmail("hack@example.com");

            mockMvc.perform(
                            put("/user/v1/keycloak/{keycloakId}", "kc-id-123")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
            verify(userApplicationService, never()).updateUser(anyString(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /user/v1/keycloak/{keycloakId}")
    class DeleteUserTests {

        @Test
        @DisplayName("204 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnNoContent_WhenAdmin() throws Exception {
            doNothing().when(userApplicationService).deleteUser("kc-id-123");

            mockMvc.perform(delete("/user/v1/keycloak/{keycloakId}", "kc-id-123"))
                    .andExpect(status().isNoContent());
            verify(userApplicationService).deleteUser("kc-id-123");
        }

        @Test
        @DisplayName("403 для RENTER, навіть якщо видаляє себе")
        @WithMockUser(
                username = "kc-id-123",
                roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(delete("/user/v1/keycloak/{keycloakId}", "kc-id-123"))
                    .andExpect(status().isForbidden());
            verify(userApplicationService, never()).deleteUser(anyString());
        }
    }

    @Nested
    @DisplayName("PATCH /user/v1/keycloak/{keycloakId}/activate")
    class ActivateUserTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.activateUser("kc-id-123")).thenReturn(sampleUserResponse());

            mockMvc.perform(patch("/user/v1/keycloak/{keycloakId}/activate", "kc-id-123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("403 для RENTER")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(patch("/user/v1/keycloak/{keycloakId}/activate", "kc-id-123"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /user/v1/keycloak/{keycloakId}/deactivate")
    class DeactivateUserTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.deactivateUser("kc-id-123"))
                    .thenReturn(sampleUserResponse());

            mockMvc.perform(patch("/user/v1/keycloak/{keycloakId}/deactivate", "kc-id-123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("403 для RENTER")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(patch("/user/v1/keycloak/{keycloakId}/deactivate", "kc-id-123"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /user/v1/exist/driverCode")
    class ExistByEmailAndDriverCodeTests {

        @Test
        @DisplayName("200 для будь-якого автентифікованого користувача")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnOk_WhenAuthenticated() throws Exception {
            when(userApplicationService.existByEmailAndDriverCode("john@example.com", "ABCD123456"))
                    .thenReturn(Optional.of(1L));

            mockMvc.perform(
                            get("/user/v1/exist/driverCode")
                                    .param("email", "john@example.com")
                                    .param("driverCode", "ABCD123456"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /user/v1/analytics/admin/active/count")
    class CountActiveUsersTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.countActiveUsers()).thenReturn(5L);

            mockMvc.perform(get("/user/v1/analytics/admin/active/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }

        @Test
        @DisplayName("403 для RENTER")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(get("/user/v1/analytics/admin/active/count"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /user/v1/analytics/admin/roles/count")
    class CountUsersByRoleTests {

        @Test
        @DisplayName("200 для ADMINISTRATOR")
        @WithMockUser(roles = {"ADMINISTRATOR"})
        void shouldReturnOk_WhenAdmin() throws Exception {
            when(userApplicationService.countByRole(UserRole.RENTER)).thenReturn(3L);

            mockMvc.perform(get("/user/v1/analytics/admin/roles/count").param("role", "RENTER"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("3"));
        }

        @Test
        @DisplayName("403 для RENTER")
        @WithMockUser(roles = {"RENTER"})
        void shouldReturnForbidden_WhenRenter() throws Exception {
            mockMvc.perform(get("/user/v1/analytics/admin/roles/count").param("role", "RENTER"))
                    .andExpect(status().isForbidden());
        }
    }
}
