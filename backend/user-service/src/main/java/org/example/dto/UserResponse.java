package org.example.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.domain.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String keycloakId;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private LocalDate createdAt;
    private LocalDateTime updatedAt;
}
