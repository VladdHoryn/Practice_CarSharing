package org.example.domain;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand cannot be empty")
    @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
    @Column(name = "brand", nullable = false)
    private String brand;

    @NotBlank(message = "Model cannot be empty")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    @Column(name = "model", nullable = false)
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1950, message = "Year must be >= 1950")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Car class is required")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "car_class", nullable = false)
    private CarClass carClass;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price per day must be greater than 0")
    @Column(name = "price_per_day", nullable = false)
    private Float pricePerDay;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Car status is required")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private CarStatus status;

    @Pattern(regexp = "^(https?://.*)?$", message = "Image URL must be valid URL")
    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CarImage> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
      createdAt = LocalDateTime.now();
      updatedAt = LocalDateTime.now();
      validateYear(); // Викликаємо валідацію перед збереженням
    }

    @PreUpdate
    protected void onUpdate() {
      updatedAt = LocalDateTime.now();
      validateYear();
    }

    private void validateYear() {
        int currentYear = Year.now().getValue();

        if (year != null && year > currentYear) {
            throw new IllegalArgumentException("Car year cannot be in the future");
        }
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

    public void changeStatus(CarStatus newStatus) {
        log.info("Car id={} status was changed from {} to {}", id, this.status, newStatus);

        this.setStatus(newStatus);
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

    public void confirmCar() {
        if (status != CarStatus.UNCONFIRMED) {
            throw new IllegalStateException("Car is not unconfirmed");
        }

        log.info("Car id={} confirmed", id);

        this.status = CarStatus.AVAILABLE;
    }

    public void cancelCar() {
        if (status != CarStatus.UNCONFIRMED) {
            throw new IllegalStateException("Car is not unconfirmed");
        }

        log.info("Car id={} canceled", id);

        this.status = CarStatus.CANCELED;
    }

    public boolean isAvailableForRent() {
        return status == CarStatus.AVAILABLE;
    }

  // =====================================================
  // МЕТОДИ ДЛЯ РОБОТИ З ФОТО
  // =====================================================

  /**
   * Додати фото до галереї
   */
  public void addImage(CarImage image) {
    image.setCar(this);
    this.images.add(image);

    // Якщо це перше фото - робимо його головним
    if (this.images.size() == 1) {
      image.setMain(true);
    }
  }

  /**
   * Отримати головне фото (main image)
   */
  public CarImage getMainImage() {
    return images.stream()
      .filter(CarImage::isMain)
      .findFirst()
      .orElse(null);
  }

  /**
   * Встановити фото як головне
   */
  public void setMainImage(Long imageId) {
    images.forEach(img -> img.setMain(false));
    images.stream()
      .filter(img -> img.getId().equals(imageId))
      .findFirst()
      .ifPresent(img -> img.setMain(true));
  }

  /**
   * Видалити фото за ID
   */
  public boolean removeImage(Long imageId) {
    CarImage imageToRemove = images.stream()
      .filter(img -> img.getId().equals(imageId))
      .findFirst()
      .orElse(null);

    if (imageToRemove != null) {
      // Якщо видаляємо головне фото - призначаємо нове
      if (imageToRemove.isMain() && images.size() > 1) {
        images.stream()
          .filter(img -> !img.getId().equals(imageId))
          .findFirst()
          .ifPresent(img -> img.setMain(true));
      }
      return images.remove(imageToRemove);
    }
    return false;
  }
}
