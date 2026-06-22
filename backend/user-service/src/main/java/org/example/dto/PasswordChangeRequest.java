package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {

  @NotBlank(message = "Password cannot be blank")
  private String newPassword;
}
