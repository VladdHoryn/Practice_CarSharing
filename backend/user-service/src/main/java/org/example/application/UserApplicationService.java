package org.example.application;

import java.util.List;
import java.util.stream.Collectors;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.exception.InvalidCredentialsException;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
  private final UserRepository userRepository;

  // CREATE
  public UserResponse createUser(UserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("User with this email already exists");
    }

    User user = new User();
    user.setKeycloakId(request.getKeycloakId());
    user.setFullName(request.getFullName());
    user.setEmail(request.getEmail());
    user.setRole(request.getRole() != null ? request.getRole() : UserRole.RENTER);

    User savedUser = userRepository.save(user);
    return mapToResponse(savedUser);
  }

  // READ ALL
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
      .map(this::mapToResponse)
      .collect(Collectors.toList());
  }

  // READ BY ID
  public UserResponse getUserById(Long id) {
    User user =
      userRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return mapToResponse(user);
  }

  public UserResponse getUserByKeycloakId(String keycloakId) {
    User user = userRepository.findByKeycloakId(keycloakId)
      .orElseThrow(() -> new RuntimeException("User not found"));
    return mapToResponse(user);
  }

  // UPDATE
  public UserResponse updateUser(String keycloakId, UserRequest request) {
    User user =
      userRepository
        .findByKeycloakId(keycloakId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.setFullName(request.getFullName());
    user.setEmail(request.getEmail());

    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    return mapToResponse(userRepository.save(user));
  }

  // DELETE (soft delete через deactivate)
  public void deleteUser(String keycloakId) {
    userRepository.deleteByKeycloakId(keycloakId);
  }

  // ACTIVATE USER
  public UserResponse activateUser(String keycloakId) {
    User user =
      userRepository
        .findByKeycloakId(keycloakId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.activate();
    return mapToResponse(userRepository.save(user));
  }

  // DEACTIVATE USER
  public UserResponse deactivateUser(String keycloakId) {
    User user =
      userRepository
        .findByKeycloakId(keycloakId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.deactivate();

    return mapToResponse(userRepository.save(user));
  }

  // MAPPER
  private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
      .id(user.getId())
      .keycloakId(user.getKeycloakId())
      .fullName(user.getFullName())
      .email(user.getEmail())
      .role(user.getRole())
      .isActive(user.isActive())
      .createdAt(user.getCreatedAt())
      .build();
  }
}
