package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(value = EnumType.STRING)
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
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;

    @Column(name = "location_city")
    private String locationCity;

    // НОВІ ПОЛЯ ДЛЯ ФОТО
    @Column(name = "images")
    private List<String> images = new ArrayList<>();

    @Column(name = "primary_image")
    private String primaryImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // =====================================================
    // Додаткові методи для роботи з фото
    // =====================================================

    /**
     * Додати фото до масиву
     */
    public void addImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageUrl);
        
        // Якщо це перше фото і primary_image ще не встановлено
        if (this.primaryImage == null && this.images.size() == 1) {
            this.primaryImage = imageUrl;
        }
    }

    /**
     * Видалити фото за індексом
     */
    public void removeImage(int index) {
        if (this.images != null && index >= 0 && index < this.images.size()) {
            String removedImage = this.images.remove(index);
            
            // Якщо видаляємо primary_image, встановлюємо нове перше фото
            if (removedImage.equals(this.primaryImage) && !this.images.isEmpty()) {
                this.primaryImage = this.images.get(0);
            } else if (this.images.isEmpty()) {
                this.primaryImage = null;
            }
        }
    }

    /**
     * Встановити головне фото
     */
    public void setPrimaryImage(String imageUrl) {
        if (this.images != null && this.images.contains(imageUrl)) {
            this.primaryImage = imageUrl;
        } else {
            throw new IllegalArgumentException("Image not found in car images list");
        }
    }

    // =====================================================
    // Бізнес-методи (вже існуючі)
    // =====================================================

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
