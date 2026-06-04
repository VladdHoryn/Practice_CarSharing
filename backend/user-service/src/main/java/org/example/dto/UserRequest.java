package org.example.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import org.example.domain.UserRole;

import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "keycloakId is required")
    private String keycloakId;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotNull(message = "Role is required")
    private UserRole role;
}
