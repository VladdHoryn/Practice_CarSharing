package org.example.repository;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.jpa.properties.hibernate.type.preferred_enum_jdbc_type=VARCHAR",
  "spring.autoconfigure.exclude=" +
    "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    savedUser = userRepository.save(createUser("John Doe", "john@example.com", UserRole.RENTER));
  }

  private User createUser(String fullName, String email, UserRole role) {
    User user = new User();
    user.setFullName(fullName);
    user.setEmail(email);
    user.setPasswordHash("hashed_password");
    user.setRole(role);
    user.setIsActive(true);
    return user;
  }

  // --- save ---

  @Test
  void save_shouldPersistUser() {
    assertThat(savedUser.getId()).isNotNull();
  }

  @Test
  void save_shouldPersistAllFields() {
    assertThat(savedUser.getFullName()).isEqualTo("John Doe");
    assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    assertThat(savedUser.getRole()).isEqualTo(UserRole.RENTER);
    assertThat(savedUser.isActive()).isTrue();
  }

  // --- findById ---

  @Test
  void findById_exists_shouldReturnUser() {
    Optional<User> found = userRepository.findById(savedUser.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("john@example.com");
  }

  @Test
  void findById_notExists_shouldReturnEmpty() {
    Optional<User> found = userRepository.findById(999L);
    assertThat(found).isEmpty();
  }

  // --- findByEmail ---

  @Test
  void findByEmail_exists_shouldReturnUser() {
    Optional<User> found = userRepository.findByEmail("john@example.com");
    assertThat(found).isPresent();
    assertThat(found.get().getFullName()).isEqualTo("John Doe");
  }

  @Test
  void findByEmail_notExists_shouldReturnEmpty() {
    Optional<User> found = userRepository.findByEmail("nobody@example.com");
    assertThat(found).isEmpty();
  }

  // --- findByFullName ---

  @Test
  void findByFullName_exists_shouldReturnUser() {
    Optional<User> found = userRepository.findByFullName("John Doe");
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("john@example.com");
  }

  @Test
  void findByFullName_notExists_shouldReturnEmpty() {
    Optional<User> found = userRepository.findByFullName("Nobody");
    assertThat(found).isEmpty();
  }

  // --- existsByEmail ---

  @Test
  void existsByEmail_exists_shouldReturnTrue() {
    assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
  }

  @Test
  void existsByEmail_notExists_shouldReturnFalse() {
    assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
  }

  // --- findAll ---

  @Test
  void findAll_shouldReturnAllUsers() {
    userRepository.save(createUser("Jane Doe", "jane@example.com", UserRole.OWNER));
    List<User> users = userRepository.findAll();
    assertThat(users).hasSize(2);
  }

  @Test
  void findAll_emptyTable_shouldReturnEmptyList() {
    userRepository.deleteAll();
    assertThat(userRepository.findAll()).isEmpty();
  }

  // --- delete ---

  @Test
  void deleteById_shouldRemoveUser() {
    userRepository.deleteById(savedUser.getId());
    assertThat(userRepository.findById(savedUser.getId())).isEmpty();
  }

  @Test
  void delete_shouldRemoveUser() {
    userRepository.delete(savedUser);
    assertThat(userRepository.findById(savedUser.getId())).isEmpty();
  }

  // --- email uniqueness ---

  @Test
  void save_duplicateEmail_shouldThrowException() {
    User duplicate = createUser("Another John", "john@example.com", UserRole.RENTER);
    assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicate));
  }

  // --- update ---

  @Test
  void save_updateUser_shouldPersistChanges() {
    savedUser.setFullName("Updated Name");
    userRepository.save(savedUser);

    Optional<User> updated = userRepository.findById(savedUser.getId());
    assertThat(updated).isPresent();
    assertThat(updated.get().getFullName()).isEqualTo("Updated Name");
  }

  // --- role ---

  @Test
  void save_userWithOwnerRole_shouldPersist() {
    User owner = userRepository.save(createUser("Owner User", "owner@example.com", UserRole.OWNER));
    assertThat(owner.getRole()).isEqualTo(UserRole.OWNER);
  }

  @Test
  void save_userWithAdminRole_shouldPersist() {
    User admin = userRepository.save(createUser("Admin User", "admin@example.com", UserRole.ADMINISTRATOR));
    assertThat(admin.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
  }

  // --- count ---

  @Test
  void count_shouldReturnCorrectNumber() {
    userRepository.save(createUser("Jane", "jane@example.com", UserRole.RENTER));
    assertThat(userRepository.count()).isEqualTo(2);
  }
}
