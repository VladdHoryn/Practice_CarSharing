package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.domain.UserRole;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
  private Long id;
  private String fullName;
  private String email;
  private UserRole role;
  private Boolean isActive;
  private LocalDate createdAt;
}
