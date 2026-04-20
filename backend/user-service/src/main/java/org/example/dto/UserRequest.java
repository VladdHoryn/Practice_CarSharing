package org.example.dto;


import lombok.Data;
import org.example.domain.UserRole;

@Data
public class UserRequest {
  private String name;
  private String password;
  private UserRole role;
}
