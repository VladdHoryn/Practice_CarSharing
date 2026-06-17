package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private Long imageId;
    private Long carId;
    private String fileName;
    private Long fileSize;
    private boolean isMain;
    private String message;
}