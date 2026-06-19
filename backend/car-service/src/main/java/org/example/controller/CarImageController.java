package org.example.controller;

import java.io.IOException;
import java.util.List;

import org.example.application.CarImageApplicationService;
import org.example.domain.CarImage;
import org.example.dto.CarImageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/car/v1/{carId}/images")
@RequiredArgsConstructor
public class CarImageController {

    private final CarImageApplicationService carImageService;

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<Void> uploadImage(
            @PathVariable Long carId, @RequestParam("file") MultipartFile file) {

        try {
            carImageService.uploadImage(carId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/main")
    public ResponseEntity<byte[]> getMainImage(@PathVariable Long carId) {
        CarImage mainImage = carImageService.getMainImage(carId);

        if (mainImage == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mainImage.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(mainImage.getContentType()))
                .body(mainImage.getImageData());
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImageById(
            @PathVariable Long carId, @PathVariable Long imageId) {

        CarImage image = carImageService.getImageById(imageId);

        if (!image.getCar().getId().equals(carId)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getImageData());
    }

    @GetMapping
    public ResponseEntity<List<CarImageResponse>> getAllImagesInfo(@PathVariable Long carId) {
        List<CarImageResponse> imagesInfo =
                carImageService.getAllImages(carId).stream()
                        .map(
                                img ->
                                        new CarImageResponse(
                                                img.getId(),
                                                img.getFileName(),
                                                img.getContentType(),
                                                img.getFileSize(),
                                                img.isMain()))
                        .toList();

        return ResponseEntity.ok(imagesInfo);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PatchMapping("/{imageId}/main")
    public ResponseEntity<Void> setMainImage(@PathVariable Long carId, @PathVariable Long imageId) {

        carImageService.setMainImage(carId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long carId, @PathVariable Long imageId) {

        carImageService.deleteImage(carId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllImages(@PathVariable Long carId) {
        carImageService.deleteAllCarImages(carId);
        return ResponseEntity.noContent().build();
    }
}
