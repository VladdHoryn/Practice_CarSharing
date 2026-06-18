package org.example.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "car_images")
public class CarImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "car_id", nullable = false)
  private Car car;

  @Lob
  @Column(name = "image_data", nullable = false)
  private byte[] imageData;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "is_main", nullable = false)
  private boolean isMain;

  @Column(name = "created_at", nullable = false, updatable = false)
  private java.time.LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = java.time.LocalDateTime.now();
  }
}
