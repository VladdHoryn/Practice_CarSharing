package org.example.dto;


import lombok.Data;
import org.example.domain.UserRole;

import java.time.LocalDate;

@Data
public class UserRequest {
  private String fullName;
  private String email;
  private String password;
  private UserRole role;
}
