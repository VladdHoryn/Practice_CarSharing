package org.example.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_documents")
@Getter
@Setter
@NoArgsConstructor
public class UserDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull(message = "Document type is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false)
  private DocumentType documentType;

  @Column(name = "file_data", nullable = false)
  private byte[] fileData;

  @NotBlank(message = "Content type is required")
  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "original_file_name")
  private String originalFileName;

  @Column(name = "is_verified", nullable = false)
  private Boolean isVerified;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @PrePersist
  protected void onUpload() {
    this.uploadedAt = LocalDateTime.now();
    if (this.isVerified == null) {
      this.isVerified = false;
    }
  }

  public void verify() {
    this.isVerified = true;
  }
}
