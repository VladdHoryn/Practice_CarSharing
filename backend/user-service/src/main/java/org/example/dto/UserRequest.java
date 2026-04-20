package org.example.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.domain.UserRole;

import java.time.LocalDate;

@Data
public class UserRequest {
  @NotBlank(message = "Full name is required")
  @Size(min = 2, max = 50, message = "Full name must be between 2 and 100 characters")
  private String fullName;
  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  private String email;
  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;
  @NotNull(message = "Role is required")
  private UserRole role;
}
