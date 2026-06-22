package org.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Response;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private Keycloak keycloak;

    @InjectMocks private UserApplicationService userApplicationService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userApplicationService, "realm", "carsharing-realm");

        user = new User();
        user.setId(1L);
        user.setKeycloakId("kc-id-123");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(UserRole.RENTER);
        user.setDriverCode("ABCD123456");
        user.setIsActive(true);
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    private void mockKeycloakCreateUser(String keycloakId) throws Exception {
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RolesResource rolesResource = mock(RolesResource.class);
        RoleResource roleResource = mock(RoleResource.class);
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

        when(keycloak.realm("carsharing-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        URI location =
                new URI(
                        "http://keycloak:8080/auth/admin/realms/carsharing-realm/users/"
                                + keycloakId);
        when(response.getLocation()).thenReturn(location);
        when(usersResource.create(any())).thenReturn(response);
        when(usersResource.get(keycloakId)).thenReturn(userResource);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(roleResource);
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("RENTER");
        when(roleResource.toRepresentation()).thenReturn(roleRep);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        doNothing().when(roleScopeResource).add(anyList());
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("успішно створює користувача")
        void shouldCreateUserSuccessfully() throws Exception {
            mockKeycloakCreateUser("kc-id-new");
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.existsByDriverCode(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            UserRequest request = new UserRequest();
            request.setEmail("new@example.com");
            request.setPassword("pass123");
            request.setFullName("Jane Smith");
            request.setRole(UserRole.RENTER);

            UserResponse result = userApplicationService.createUser(request);
            assertThat(result).isNotNull();
            verify(userRepository).save(any());
        }

        @Test
        @DisplayName("кидає RuntimeException якщо email вже існує")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            UserRequest request = new UserRequest();
            request.setEmail("john@example.com");
            request.setPassword("pass");
            request.setFullName("John Doe");
            request.setRole(UserRole.RENTER);

            assertThatThrownBy(() -> userApplicationService.createUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already exists");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("кидає RuntimeException якщо Keycloak повертає не 201")
        void shouldThrowWhenKeycloakFails() throws Exception {
            RealmResource realmResource = mock(RealmResource.class);
            UsersResource usersResource = mock(UsersResource.class);
            when(keycloak.realm("carsharing-realm")).thenReturn(realmResource);
            when(realmResource.users()).thenReturn(usersResource);

            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(409);
            when(usersResource.create(any())).thenReturn(response);
            when(userRepository.existsByEmail("fail@example.com")).thenReturn(false);

            UserRequest request = new UserRequest();
            request.setEmail("fail@example.com");
            request.setPassword("pass");
            request.setFullName("Fail User");
            request.setRole(UserRole.RENTER);

            assertThatThrownBy(() -> userApplicationService.createUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create user in Keycloak");
        }

        @Test
        @DisplayName("призначає RENTER роль за замовчуванням якщо роль null")
        void shouldDefaultToRenterRole() throws Exception {
            mockKeycloakCreateUser("kc-id-default");
            when(userRepository.existsByEmail("default@example.com")).thenReturn(false);
            when(userRepository.existsByDriverCode(anyString())).thenReturn(false);

            User savedUser = new User();
            savedUser.setId(2L);
            savedUser.setKeycloakId("kc-id-default");
            savedUser.setRole(UserRole.RENTER);
            savedUser.setIsActive(true);
            when(userRepository.save(any()))
                    .thenAnswer(
                            inv -> {
                                User u = inv.getArgument(0);
                                assertThat(u.getRole()).isEqualTo(UserRole.RENTER);
                                return savedUser;
                            });

            UserRequest request = new UserRequest();
            request.setEmail("default@example.com");
            request.setPassword("pass");
            request.setFullName("Default User");
            request.setRole(null);

            userApplicationService.createUser(request);
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsersTests {

        @Test
        @DisplayName("повертає всіх користувачів")
        void shouldReturnAllUsers() {

            User secondUser = new User();
            secondUser.setId(2L);
            secondUser.setKeycloakId("kc-id-456");
            secondUser.setFullName("Second User");
            secondUser.setEmail("second@example.com");
            secondUser.setRole(UserRole.RENTER);
            secondUser.setIsActive(false);
            secondUser.setCreatedAt(LocalDate.now());
            secondUser.setUpdatedAt(LocalDateTime.now());

            when(userRepository.findAll()).thenReturn(List.of(user, secondUser));
            List<UserResponse> result = userApplicationService.getAllUsers();
            assertThat(result).hasSize(2);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("повертає порожній список якщо немає користувачів")
        void shouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            assertThat(userApplicationService.getAllUsers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserByIdTests {

        @Test
        @DisplayName("повертає користувача за ID")
        void shouldReturnUserById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            UserResponse result = userApplicationService.getUserById(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("кидає RuntimeException якщо не знайдено")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userApplicationService.getUserById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getUserByKeycloakId()")
    class GetUserByKeycloakIdTests {

        @Test
        @DisplayName("повертає користувача за keycloakId")
        void shouldReturnUserByKeycloakId() {
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            UserResponse result = userApplicationService.getUserByKeycloakId("kc-id-123");
            assertThat(result.getKeycloakId()).isEqualTo("kc-id-123");
        }

        @Test
        @DisplayName("кидає RuntimeException якщо не знайдено")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userApplicationService.getUserByKeycloakId("unknown"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateUserTests {

        @Test
        @DisplayName("оновлює ім'я та email")
        void shouldUpdateUser() {
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequest request = new UserRequest();
            request.setFullName("Updated Name");
            request.setEmail("updated@example.com");
            request.setPassword("pass");
            request.setRole(null);

            UserResponse result = userApplicationService.updateUser("kc-id-123", request);
            assertThat(result.getFullName()).isEqualTo("Updated Name");
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
        }

        @Test
        @DisplayName("оновлює роль якщо задана")
        void shouldUpdateRoleWhenProvided() {
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequest request = new UserRequest();
            request.setFullName("John Doe");
            request.setEmail("john@example.com");
            request.setPassword("pass");
            request.setRole(UserRole.OWNER);

            UserResponse result = userApplicationService.updateUser("kc-id-123", request);
            assertThat(result.getRole()).isEqualTo(UserRole.OWNER);
        }

        @Test
        @DisplayName("не змінює роль якщо вона null")
        void shouldNotChangeRoleWhenNull() {
            user.setRole(UserRole.RENTER);
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserRequest request = new UserRequest();
            request.setFullName("John Doe");
            request.setEmail("john@example.com");
            request.setPassword("pass");
            request.setRole(null);

            UserResponse result = userApplicationService.updateUser("kc-id-123", request);
            assertThat(result.getRole()).isEqualTo(UserRole.RENTER);
        }

        @Test
        @DisplayName("кидає RuntimeException якщо не знайдено")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

            UserRequest request = new UserRequest();
            request.setFullName("Name");
            request.setEmail("e@e.com");
            request.setPassword("pass");

            assertThatThrownBy(() -> userApplicationService.updateUser("unknown", request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("видаляє користувача за keycloakId")
        void shouldDeleteUser() {
            doNothing().when(userRepository).deleteByKeycloakId("kc-id-123");
            userApplicationService.deleteUser("kc-id-123");
            verify(userRepository).deleteByKeycloakId("kc-id-123");
        }
    }

    @Nested
    @DisplayName("activateUser()")
    class ActivateUserTests {

        @Test
        @DisplayName("активує користувача")
        void shouldActivateUser() {
            user.setIsActive(false);
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UserResponse result = userApplicationService.activateUser("kc-id-123");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("кидає RuntimeException якщо не знайдено")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userApplicationService.activateUser("unknown"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("deactivateUser()")
    class DeactivateUserTests {

        @Test
        @DisplayName("деактивує користувача")
        void shouldDeactivateUser() {
            user.setIsActive(true);
            when(userRepository.findByKeycloakId("kc-id-123")).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UserResponse result = userApplicationService.deactivateUser("kc-id-123");
            assertThat(result.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("кидає RuntimeException якщо не знайдено")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userApplicationService.deactivateUser("unknown"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("existByEmailAndDriverCode()")
    class ExistByEmailAndDriverCodeTests {

        @Test
        @DisplayName("повертає Optional з ID якщо знайдено")
        void shouldReturnIdWhenFound() {
            when(userRepository.findIdByEmailAndDriverCode("john@example.com", "ABCD123456"))
                    .thenReturn(Optional.of(1L));
            Optional<Long> result =
                    userApplicationService.existByEmailAndDriverCode(
                            "john@example.com", "ABCD123456");
            assertThat(result).isPresent().contains(1L);
        }

        @Test
        @DisplayName("повертає порожній Optional якщо не знайдено")
        void shouldReturnEmptyWhenNotFound() {
            when(userRepository.findIdByEmailAndDriverCode("no@example.com", "XXXX999999"))
                    .thenReturn(Optional.empty());
            assertThat(
                            userApplicationService.existByEmailAndDriverCode(
                                    "no@example.com", "XXXX999999"))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("countActiveUsers()")
    class CountActiveUsersTests {

        @Test
        @DisplayName("повертає кількість активних користувачів")
        void shouldCountActiveUsers() {
            when(userRepository.countActiveUsers()).thenReturn(5L);
            assertThat(userApplicationService.countActiveUsers()).isEqualTo(5L);
        }

        @Test
        @DisplayName("повертає 0 якщо немає активних")
        void shouldReturnZeroWhenNoActive() {
            when(userRepository.countActiveUsers()).thenReturn(0L);
            assertThat(userApplicationService.countActiveUsers()).isZero();
        }
    }

    @Nested
    @DisplayName("countByRole()")
    class CountByRoleTests {

        @Test
        @DisplayName("повертає кількість за роллю RENTER")
        void shouldCountByRole() {
            when(userRepository.countByRole(UserRole.RENTER)).thenReturn(3L);
            assertThat(userApplicationService.countByRole(UserRole.RENTER)).isEqualTo(3L);
        }

        @Test
        @DisplayName("повертає 0 якщо роль null")
        void shouldReturnZeroWhenRoleIsNull() {
            assertThat(userApplicationService.countByRole(null)).isZero();
            verify(userRepository, never()).countByRole(any());
        }

        @Test
        @DisplayName("повертає кількість для OWNER")
        void shouldCountOwners() {
            when(userRepository.countByRole(UserRole.OWNER)).thenReturn(2L);
            assertThat(userApplicationService.countByRole(UserRole.OWNER)).isEqualTo(2L);
        }
    }
}
