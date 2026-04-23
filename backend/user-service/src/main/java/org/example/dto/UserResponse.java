package org.example.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.example.domain.UserRole;

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
