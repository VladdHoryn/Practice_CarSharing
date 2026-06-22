package org.example.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.example.application.UserApplicationService;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/v1")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserApplicationService userService;

    @Operation(summary = "Register user", description = "Creates a new user in the system and Keycloak. Open endpoint.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Email already exists or invalid data")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Returns all registered users. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users returned"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by Keycloak ID", description = "Accessible by the user themselves or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#keycloakId == authentication.name or hasRole('ADMINISTRATOR')")
    @GetMapping("keycloak/{keycloakId}")
    public ResponseEntity<UserResponse> getUserByKeycloakId(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.getUserByKeycloakId(keycloakId));
    }

    @Operation(summary = "Update user", description = "Updates user profile. Accessible by the user themselves or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("#keycloakId == authentication.name or hasRole('ADMINISTRATOR')")
    @PutMapping("keycloak/{keycloakId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String keycloakId, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(keycloakId, request));
    }

    @Operation(summary = "Delete user", description = "Deletes a user by Keycloak ID. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("keycloak/{keycloakId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakId) {
        userService.deleteUser(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate user", description = "Activates a deactivated user account. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User activated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PatchMapping("keycloak/{keycloakId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.activateUser(keycloakId));
    }

    @Operation(summary = "Deactivate user", description = "Deactivates a user account. Accessible by ADMINISTRATOR only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deactivated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PatchMapping("keycloak/{keycloakId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.deactivateUser(keycloakId));
    }

    @Operation(summary = "Check user by email and driver code", description = "Returns user ID if a user with the given email and driver code exists. Used internally by booking-service.")
    @ApiResponse(responseCode = "200", description = "User ID returned or empty if not found")
    @GetMapping("/exist/driverCode")
    public Optional<Long> userExistWithEmailAndDriverCode(
            @RequestParam String email, @RequestParam String driverCode) {
        return userService.existByEmailAndDriverCode(email, driverCode);
    }

    @Operation(summary = "Count active users (admin)")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/active/count")
    public ResponseEntity<Long> countActiveUsers() {
        return ResponseEntity.ok(userService.countActiveUsers());
    }

    @Operation(summary = "Count users by role (admin)")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/analytics/admin/roles/count")
    public ResponseEntity<Long> countUsersByRole(@RequestParam org.example.domain.UserRole role) {
        return ResponseEntity.ok(userService.countByRole(role));
    }
}
