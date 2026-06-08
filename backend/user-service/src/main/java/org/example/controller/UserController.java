package org.example.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.example.application.UserApplicationService;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

//    @PreAuthorize("authentication.name")
//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
//        return ResponseEntity.ok(userService.getUserById(id));
//    }

  @PreAuthorize("#keycloakId == authentication.name or hasRole('ADMINISTRATOR')")
  @GetMapping("keycloak/{keycloakId}")
  public ResponseEntity<UserResponse> getUserByKeycloakId(@PathVariable String keycloakId) {
    return ResponseEntity.ok(userService.getUserByKeycloakId(keycloakId));
  }

    @PutMapping("/{keycloakId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String keycloakId, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(keycloakId, request));
    }

    @DeleteMapping("/{keycloakId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakId) {
        userService.deleteUser(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{keycloakId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.activateUser(keycloakId));
    }

    @PatchMapping("/{keycloakId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.deactivateUser(keycloakId));
    }
}
