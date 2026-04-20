package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Year;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Car {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank(message = "Brand cannot be empty")
  @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
  @Column(name = "brand", nullable = false)
  public String brand;

  @NotBlank(message = "Model cannot be empty")
  @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
  @Column(name = "model", nullable = false)
  public String model;

  @NotNull(message = "Year is required")
  @Min(value = 1950, message = "Year must be >= 1950")
  @Column(name = "year", nullable = false)
  public Integer year;

  @NotNull(message = "Car class is required")
  @Enumerated(value = EnumType.STRING)
  @Column(name = "car_class", nullable = false)
  public CarClass carClass;

  @NotNull(message = "Price per day is required")
  @Positive(message = "Price per day must be greater than 0")
  @Column(name = "price_per_day", nullable = false)
  public Float pricePerDay;

  @NotNull(message = "User ID is required")
  @Column(name = "user_id", nullable = false)
  public Long userId;

  @NotNull(message = "Car status is required")
  @Enumerated(value = EnumType.STRING)
  @Column(name = "status", nullable = false)
  public CarStatus status;

  @Pattern(
    regexp = "^(https?://.*)?$",
    message = "Image URL must be valid URL"
  )
  @Column(name = "image_url")
  public String imageUrl;

  @PrePersist
  @PreUpdate
  private void validateYear() {
    int currentYear = Year.now().getValue();

    if (year != null && year > currentYear) {
      throw new IllegalArgumentException("Car year cannot be in the future");
    }
  }

  public void markAsAvailable() {
    log.info("Car id={} -> AVAILABLE", id);

    if (status == CarStatus.RENTED) {
      throw new IllegalStateException("Cannot make rented car directly available");
    }

    this.status = CarStatus.AVAILABLE;
  }

  public void rent(Long renterId) {
    log.info("Car id={} rented by userId={}", id, renterId);

    if (status != CarStatus.AVAILABLE) {
      throw new IllegalStateException("Car is not available for rent");
    }

    if (renterId == null) {
      throw new IllegalArgumentException("Renter id cannot be null");
    }

    this.status = CarStatus.RENTED;
    this.userId = renterId;
  }

  public void returnFromRent() {
    log.info("Car id={} returned from rent", id);

    if (status != CarStatus.RENTED) {
      throw new IllegalStateException("Only rented cars can be returned");
    }

    this.status = CarStatus.AVAILABLE;
    this.userId = null;
  }

  public void sendToMaintenance() {
    log.info("Car id={} sent to maintenance", id);

    if (status == CarStatus.RENTED) {
      throw new IllegalStateException("Cannot send rented car to maintenance");
    }

    this.status = CarStatus.MAINTENANCE;
  }

  public void completeMaintenance() {
    log.info("Car id={} maintenance completed", id);

    if (status != CarStatus.MAINTENANCE) {
      throw new IllegalStateException("Car is not in maintenance");
    }

    this.status = CarStatus.AVAILABLE;
  }

  public boolean isAvailableForRent() {
    return status == CarStatus.AVAILABLE;
  }
}
