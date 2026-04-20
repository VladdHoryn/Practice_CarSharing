package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.domain.UserRole;

@Data
@Builder
public class UserResponse {
  private Long id;
  private String name;
  private UserRole role;
}
