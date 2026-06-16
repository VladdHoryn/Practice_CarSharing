package org.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setFullName("John Doe");
    user.setEmail("john@example.com");
    user.setPasswordHash("hashed_password");
    user.setRole(UserRole.RENTER);
    user.setIsActive(true);
  }

  // --- isActive ---

  @Test
  void isActive_whenTrue_shouldReturnTrue() {
    user.setIsActive(true);
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void isActive_whenFalse_shouldReturnFalse() {
    user.setIsActive(false);
    assertThat(user.isActive()).isFalse();
  }

  // --- activate ---

  @Test
  void activate_whenDeactivated_shouldBecomeActive() {
    user.setIsActive(false);
    user.activate();
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void activate_whenAlreadyActive_shouldRemainActive() {
    user.setIsActive(true);
    user.activate();
    assertThat(user.isActive()).isTrue();
  }

  // --- deactivate ---

  @Test
  void deactivate_whenActive_shouldBecomeInactive() {
    user.setIsActive(true);
    user.deactivate();
    assertThat(user.isActive()).isFalse();
  }

  @Test
  void deactivate_whenAlreadyDeactivated_shouldRemainInactive() {
    user.setIsActive(false);
    user.deactivate();
    assertThat(user.isActive()).isFalse();
  }

  // --- onCreate (PrePersist) ---

  @Test
  void onCreate_shouldSetCreatedAt() {
    User newUser = new User();
    newUser.setFullName("Jane Doe");
    newUser.setEmail("jane@example.com");
    newUser.setPasswordHash("hash");
    newUser.setRole(UserRole.OWNER);

    // simulate @PrePersist
    newUser.onCreate();

    assertThat(newUser.getCreatedAt()).isNotNull();
    assertThat(newUser.getCreatedAt()).isEqualTo(LocalDate.now());
  }

  @Test
  void onCreate_shouldSetUpdatedAt() {
    User newUser = new User();
    newUser.onCreate();

    assertThat(newUser.getUpdatedAt()).isNotNull();
    assertThat(newUser.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
  }

  @Test
  void onCreate_shouldSetIsActiveToTrue() {
    User newUser = new User();
    newUser.onCreate();

    assertThat(newUser.isActive()).isTrue();
  }

  // --- UserRole ---

  @Test
  void role_renter_shouldBeSet() {
    user.setRole(UserRole.RENTER);
    assertThat(user.getRole()).isEqualTo(UserRole.RENTER);
  }

  @Test
  void role_owner_shouldBeSet() {
    user.setRole(UserRole.OWNER);
    assertThat(user.getRole()).isEqualTo(UserRole.OWNER);
  }

  @Test
  void role_administrator_shouldBeSet() {
    user.setRole(UserRole.ADMINISTRATOR);
    assertThat(user.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
  }

  // --- getters/setters ---

  @Test
  void setFullName_shouldUpdateField() {
    user.setFullName("Jane Smith");
    assertThat(user.getFullName()).isEqualTo("Jane Smith");
  }

  @Test
  void setEmail_shouldUpdateField() {
    user.setEmail("newemail@example.com");
    assertThat(user.getEmail()).isEqualTo("newemail@example.com");
  }

  @Test
  void setPasswordHash_shouldUpdateField() {
    user.setPasswordHash("new_hash");
    assertThat(user.getPasswordHash()).isEqualTo("new_hash");
  }

  // --- toggles ---

  @Test
  void deactivateThenActivate_shouldReturnToActive() {
    user.deactivate();
    assertThat(user.isActive()).isFalse();
    user.activate();
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void activateThenDeactivate_shouldBecomeInactive() {
    user.activate();
    assertThat(user.isActive()).isTrue();
    user.deactivate();
    assertThat(user.isActive()).isFalse();
  }
}
