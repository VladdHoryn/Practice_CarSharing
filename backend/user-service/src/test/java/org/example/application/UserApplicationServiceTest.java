package org.example.application;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.exception.InvalidCredentialsException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @InjectMocks
  private UserApplicationService userService;

  private User user;
  private UserRequest userRequest;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setFullName("John Doe");
    user.setEmail("john@example.com");
    user.setPasswordHash("$2a$10$hashedPassword");
    user.setRole(UserRole.RENTER);
    user.setIsActive(true);
    user.setCreatedAt(LocalDate.now());

    userRequest = new UserRequest();
    userRequest.setFullName("John Doe");
    userRequest.setEmail("john@example.com");
    userRequest.setPassword("password123");
    userRequest.setRole(UserRole.RENTER);
  }

  // ===================== REGISTER =====================

  @Test
  void register_newEmail_shouldReturnAuthResponse() {
    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
    when(userRepository.save(any(User.class))).thenReturn(user);

    AuthResponse response = userService.register(userRequest);

    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo("Registration successful");
    assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void register_defaultRole_whenRoleIsNull_shouldSetRenter() {
    userRequest.setRole(null);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashed");

    User savedUser = new User();
    savedUser.setId(2L);
    savedUser.setFullName("John Doe");
    savedUser.setEmail("john@example.com");
    savedUser.setPasswordHash("hashed");
    savedUser.setRole(UserRole.RENTER);
    savedUser.setIsActive(true);
    savedUser.setCreatedAt(LocalDate.now());

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    AuthResponse response = userService.register(userRequest);

    assertThat(response.getUser().getRole()).isEqualTo(UserRole.RENTER);
  }

  @Test
  void register_duplicateEmail_shouldThrowRuntimeException() {
    when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(userRequest))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User with this email already exists");

    verify(userRepository, never()).save(any());
  }

  @Test
  void register_shouldEncodePassword() {
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashed");
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.register(userRequest);

    verify(passwordEncoder).encode("password123");
  }

  // ===================== LOGIN =====================

  @Test
  void login_validCredentials_shouldReturnAuthResponse() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("john@example.com");
    loginRequest.setPassword("password123");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

    AuthResponse response = userService.login(loginRequest);

    assertThat(response.getMessage()).isEqualTo("Login successful");
    assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
  }

  @Test
  void login_wrongEmail_shouldThrowInvalidCredentialsException() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("nobody@example.com");
    loginRequest.setPassword("password123");

    when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(loginRequest))
      .isInstanceOf(InvalidCredentialsException.class)
      .hasMessageContaining("Invalid email or password");
  }

  @Test
  void login_wrongPassword_shouldThrowInvalidCredentialsException() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("john@example.com");
    loginRequest.setPassword("wrongpassword");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpassword", user.getPasswordHash())).thenReturn(false);

    assertThatThrownBy(() -> userService.login(loginRequest))
      .isInstanceOf(InvalidCredentialsException.class)
      .hasMessageContaining("Invalid email or password");
  }

  @Test
  void login_deactivatedAccount_shouldThrowRuntimeException() {
    user.setIsActive(false);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("john@example.com");
    loginRequest.setPassword("password123");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.login(loginRequest))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Account is deactivated");
  }

  // ===================== CREATE USER =====================

  @Test
  void createUser_newEmail_shouldReturnUserResponse() {
    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse response = userService.createUser(userRequest);

    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo("john@example.com");
    assertThat(response.getFullName()).isEqualTo("John Doe");
  }

  @Test
  void createUser_duplicateEmail_shouldThrow() {
    when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.createUser(userRequest))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User with this email already exists");

    verify(userRepository, never()).save(any());
  }

  @Test
  void createUser_nullRole_shouldDefaultToRenter() {
    userRequest.setRole(null);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashed");

    User savedUser = new User();
    savedUser.setId(2L);
    savedUser.setFullName("John Doe");
    savedUser.setEmail("john@example.com");
    savedUser.setPasswordHash("hashed");
    savedUser.setRole(UserRole.RENTER);
    savedUser.setIsActive(true);
    savedUser.setCreatedAt(LocalDate.now());

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserResponse response = userService.createUser(userRequest);

    assertThat(response.getRole()).isEqualTo(UserRole.RENTER);
  }

  // ===================== GET ALL USERS =====================

  @Test
  void getAllUsers_shouldReturnMappedList() {
    User user2 = new User();
    user2.setId(2L);
    user2.setFullName("Jane Doe");
    user2.setEmail("jane@example.com");
    user2.setRole(UserRole.OWNER);
    user2.setIsActive(true);
    user2.setCreatedAt(LocalDate.now());

    when(userRepository.findAll()).thenReturn(List.of(user, user2));

    List<UserResponse> result = userService.getAllUsers();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
    assertThat(result.get(1).getEmail()).isEqualTo("jane@example.com");
  }

  @Test
  void getAllUsers_emptyList_shouldReturnEmpty() {
    when(userRepository.findAll()).thenReturn(List.of());

    List<UserResponse> result = userService.getAllUsers();

    assertThat(result).isEmpty();
  }

  // ===================== GET USER BY ID =====================

  @Test
  void getUserById_exists_shouldReturnUserResponse() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserResponse response = userService.getUserById(1L);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getEmail()).isEqualTo("john@example.com");
  }

  @Test
  void getUserById_notFound_shouldThrowRuntimeException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(999L))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User not found");
  }

  // ===================== UPDATE USER =====================

  @Test
  void updateUser_validRequest_shouldUpdateFields() {
    UserRequest updateRequest = new UserRequest();
    updateRequest.setFullName("Updated Name");
    updateRequest.setEmail("updated@example.com");
    updateRequest.setPassword("newpassword");
    updateRequest.setRole(UserRole.OWNER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newpassword")).thenReturn("new_hashed");

    User updatedUser = new User();
    updatedUser.setId(1L);
    updatedUser.setFullName("Updated Name");
    updatedUser.setEmail("updated@example.com");
    updatedUser.setPasswordHash("new_hashed");
    updatedUser.setRole(UserRole.OWNER);
    updatedUser.setIsActive(true);
    updatedUser.setCreatedAt(LocalDate.now());

    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    UserResponse response = userService.updateUser(1L, updateRequest);

    assertThat(response.getFullName()).isEqualTo("Updated Name");
    assertThat(response.getEmail()).isEqualTo("updated@example.com");
    assertThat(response.getRole()).isEqualTo(UserRole.OWNER);
  }

  @Test
  void updateUser_nullPassword_shouldNotEncodePassword() {
    UserRequest updateRequest = new UserRequest();
    updateRequest.setFullName("Updated Name");
    updateRequest.setEmail("john@example.com");
    updateRequest.setPassword(null);
    updateRequest.setRole(UserRole.RENTER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.updateUser(1L, updateRequest);

    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void updateUser_blankPassword_shouldNotEncodePassword() {
    UserRequest updateRequest = new UserRequest();
    updateRequest.setFullName("Updated Name");
    updateRequest.setEmail("john@example.com");
    updateRequest.setPassword("   ");
    updateRequest.setRole(UserRole.RENTER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.updateUser(1L, updateRequest);

    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void updateUser_notFound_shouldThrowRuntimeException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateUser(999L, userRequest))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User not found");
  }

  @Test
  void updateUser_nullRole_shouldNotUpdateRole() {
    UserRequest updateRequest = new UserRequest();
    updateRequest.setFullName("Updated Name");
    updateRequest.setEmail("john@example.com");
    updateRequest.setPassword(null);
    updateRequest.setRole(null);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse response = userService.updateUser(1L, updateRequest);

    assertThat(user.getRole()).isEqualTo(UserRole.RENTER); // unchanged
  }

  // ===================== DELETE USER =====================

  @Test
  void deleteUser_shouldCallDeleteById() {
    doNothing().when(userRepository).deleteById(1L);

    userService.deleteUser(1L);

    verify(userRepository).deleteById(1L);
  }

  // ===================== ACTIVATE USER =====================

  @Test
  void activateUser_deactivatedUser_shouldBecomeActive() {
    user.setIsActive(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User activatedUser = new User();
    activatedUser.setId(1L);
    activatedUser.setFullName("John Doe");
    activatedUser.setEmail("john@example.com");
    activatedUser.setRole(UserRole.RENTER);
    activatedUser.setIsActive(true);
    activatedUser.setCreatedAt(LocalDate.now());

    when(userRepository.save(any(User.class))).thenReturn(activatedUser);

    UserResponse response = userService.activateUser(1L);

    assertThat(response.getIsActive()).isTrue();
  }

  @Test
  void activateUser_notFound_shouldThrowRuntimeException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.activateUser(999L))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User not found");
  }

  // ===================== DEACTIVATE USER =====================

  @Test
  void deactivateUser_activeUser_shouldBecomeInactive() {
    user.setIsActive(true);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User deactivatedUser = new User();
    deactivatedUser.setId(1L);
    deactivatedUser.setFullName("John Doe");
    deactivatedUser.setEmail("john@example.com");
    deactivatedUser.setRole(UserRole.RENTER);
    deactivatedUser.setIsActive(false);
    deactivatedUser.setCreatedAt(LocalDate.now());

    when(userRepository.save(any(User.class))).thenReturn(deactivatedUser);

    UserResponse response = userService.deactivateUser(1L);

    assertThat(response.getIsActive()).isFalse();
  }

  @Test
  void deactivateUser_notFound_shouldThrowRuntimeException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deactivateUser(999L))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("User not found");
  }
}
