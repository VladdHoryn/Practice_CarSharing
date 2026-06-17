package org.example.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.DriverCodeGenerator;
import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.repository.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {
    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    // CREATE
    @Transactional
    public UserResponse createUser(UserRequest request) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("User with this email already exists");
      }

      UserRepresentation kcUser = new UserRepresentation();
      kcUser.setUsername(request.getEmail());
      kcUser.setEmail(request.getEmail());
      kcUser.setEnabled(true);
      kcUser.setEmailVerified(true);

      String[] names = request.getFullName().split(" ", 2);
      kcUser.setFirstName(names[0]);
      if (names.length > 1) {
        kcUser.setLastName(names[1]);
      }

      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType(CredentialRepresentation.PASSWORD);
      credential.setValue(request.getPassword());
      credential.setTemporary(false);
      kcUser.setCredentials(Collections.singletonList(credential));

      UsersResource usersResource = keycloak.realm(realm).users();
      Response response = usersResource.create(kcUser);

      if (response.getStatus() != 201) {
        throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
      }

      String path = response.getLocation().getPath();
      String keycloakId = path.substring(path.lastIndexOf('/') + 1);

      UserRole role = request.getRole() != null ? request.getRole() : UserRole.RENTER;

      assignRealmRoleToUser(keycloakId, role);

      User user = new User();
      user.setKeycloakId(keycloakId);
      user.setFullName(request.getFullName());
      user.setEmail(request.getEmail());
      user.setRole(role);
      user.setDriverCode(generateUniqueDriverCode());
      user.setIsActive(true);
      user.setCreatedAt(LocalDate.now());
      user.setUpdatedAt(LocalDateTime.now());

      User savedUser = userRepository.save(user);
      return mapToResponse(savedUser);
    }

  private void assignRealmRoleToUser(String keycloakId, UserRole role) {
    try {
      RealmResource realmResource = keycloak.realm(realm);

      UserResource userResource = realmResource.users().get(keycloakId);

      RoleRepresentation realmRole = realmResource.roles()
        .get(role.name())
        .toRepresentation();

      userResource.roles()
        .realmLevel()
        .add(Collections.singletonList(realmRole));

      log.info("Role {} successfully assigned to user {} in Keycloak", role.name(), keycloakId);

    } catch (NotFoundException e) {
      log.error("Role {} not found in Keycloak realm {}", role.name(), realm);
      throw new RuntimeException("Failed to assign role: Role not found in Keycloak");
    } catch (Exception e) {
      log.error("Error assigning role {} to user {}: {}", role.name(), keycloakId, e.getMessage());
      throw new RuntimeException("Failed to assign role in Keycloak", e);
    }
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
        User user =
                userRepository
                        .findByKeycloakId(keycloakId)
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

    private String generateUniqueDriverCode() {

        String code;

        do {
            code = DriverCodeGenerator.generate();
        } while (userRepository.existsByDriverCode(code));

        return code;
    }

    public Optional<Long> existByEmailAndDriverCode(String email, String driverCode) {
        return userRepository.findIdByEmailAndDriverCode(email, driverCode);
    }
}
