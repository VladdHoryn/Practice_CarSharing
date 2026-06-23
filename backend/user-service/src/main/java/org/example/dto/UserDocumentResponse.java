package org.example.dto;

import java.time.LocalDateTime;

import org.example.domain.DocumentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDocumentResponse {
    private Long id;
    private Long userId;
    private DocumentType documentType;
    private String originalFileName;
    private String contentType;
    private Boolean isVerified;
    private LocalDateTime uploadedAt;
}
