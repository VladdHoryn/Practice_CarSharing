package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Full name is required")
  @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
  @Column(name = "full_name", nullable = false)
  private String fullName;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  @Column(name = "password", nullable = false)
  private String password;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @NotNull(message = "Role is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDate createdAt;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDate.now();
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
  }

  public void activate() {
    this.isActive = true;
  }

  public boolean isActive() {
    return Boolean.TRUE.equals(this.isActive);
  }
}
