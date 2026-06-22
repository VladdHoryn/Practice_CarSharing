package org.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
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

    @Test
    void shouldSetAndGetId() {
        user.setId(99L);
        assertThat(user.getId()).isEqualTo(99L);
    }

    @Test
    void shouldSetAndGetKeycloakId() {
        user.setKeycloakId("new-kc-id");
        assertThat(user.getKeycloakId()).isEqualTo("new-kc-id");
    }

    @Test
    void shouldSetAndGetFullName() {
        user.setFullName("Jane Smith");
        assertThat(user.getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    void shouldSetAndGetEmail() {
        user.setEmail("jane@example.com");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void shouldSetAndGetRole() {
        user.setRole(UserRole.OWNER);
        assertThat(user.getRole()).isEqualTo(UserRole.OWNER);
    }

    @Test
    void shouldSetAndGetDriverCode() {
        user.setDriverCode("ZZZZ999999");
        assertThat(user.getDriverCode()).isEqualTo("ZZZZ999999");
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    @DisplayName("підтримує всі ролі користувача")
    void shouldSupportAllRoles(UserRole role) {
        user.setRole(role);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Nested
    @DisplayName("activate() / deactivate() / isActive()")
    class ActivationTests {

        @Test
        @DisplayName("activate() встановлює isActive = true")
        void shouldActivateUser() {
            user.setIsActive(false);
            user.activate();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("deactivate() встановлює isActive = false")
        void shouldDeactivateUser() {
            user.setIsActive(true);
            user.deactivate();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("isActive() повертає true для активного користувача")
        void shouldReturnTrueWhenActive() {
            user.setIsActive(true);
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive() повертає false для неактивного користувача")
        void shouldReturnFalseWhenInactive() {
            user.setIsActive(false);
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("повторна активація не змінює стан")
        void shouldAllowRepeatedActivation() {
            user.setIsActive(true);
            user.activate();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("повторна деактивація не змінює стан")
        void shouldAllowRepeatedDeactivation() {
            user.setIsActive(false);
            user.deactivate();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("активація після деактивації повертає користувача до активного стану")
        void shouldReactivateAfterDeactivation() {
            user.deactivate();
            assertThat(user.isActive()).isFalse();
            user.activate();
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("onCreate()")
    class OnCreateTests {

        @Test
        @DisplayName("встановлює createdAt і updatedAt")
        void shouldSetTimestamps() {
            User u = new User();
            u.onCreate();
            assertThat(u.getCreatedAt()).isNotNull();
            assertThat(u.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("встановлює isActive = true при першому збереженні")
        void shouldSetIsActiveToTrue() {
            User u = new User();
            u.onCreate();
            assertThat(u.isActive()).isTrue();
        }
    }
}
