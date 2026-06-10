package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "booking_drivers")
@Getter
@Setter
@NoArgsConstructor
public class BookingDriver {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Booking id is required")
  @Column(name = "booking_id", nullable = false)
  private Long bookingId;

  @NotNull(message = "User id is required")
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Column(name = "email", nullable = false)
  private String email;

  @NotBlank(message = "Driver code is required")
  @Size(min = 10, max = 10, message = "Driver code must contain exactly 10 characters")
  @Column(name = "driver_code", nullable = false, length = 10)
  private String driverCode;

  @NotNull(message = "Status is required")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(
    name = "status",
    nullable = false,
    columnDefinition = "booking_driver_status_enum"
  )
  private BookingDriverStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false, updatable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();

    if (this.status == null) {
      this.status = BookingDriverStatus.PENDING;
    }
  }

  public void accept() {
    this.status = BookingDriverStatus.ACCEPTED;
  }

  public void decline() {
    this.status = BookingDriverStatus.DECLINED;
  }

  public boolean isPending() {
    return this.status == BookingDriverStatus.PENDING;
  }

  public boolean isAccepted() {
    return this.status == BookingDriverStatus.ACCEPTED;
  }

  public boolean isDeclined() {
    return this.status == BookingDriverStatus.DECLINED;
  }
}
