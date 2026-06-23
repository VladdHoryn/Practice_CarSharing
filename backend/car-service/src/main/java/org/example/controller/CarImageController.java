package org.example.controller;

import java.io.IOException;
import java.util.List;

import org.example.application.CarImageApplicationService;
import org.example.domain.CarImage;
import org.example.dto.CarImageResponse;
import org.example.dto.OwnerMainImageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/car/v1")
@RequiredArgsConstructor
@Tag(name = "Car Images", description = "Car image upload and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CarImageController {

    private final CarImageApplicationService carImageService;

    @Operation(
            summary = "Upload car image",
            description =
                    "Uploads an image for the specified car. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image uploaded"),
        @ApiResponse(responseCode = "500", description = "Failed to process image"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PostMapping("/{carId}/images")
    public ResponseEntity<Void> uploadImage(
            @PathVariable Long carId, @RequestParam("file") MultipartFile file) {

        try {
            carImageService.uploadImage(carId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Get main image",
            description = "Returns the main image binary for the specified car.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image returned"),
        @ApiResponse(responseCode = "404", description = "No main image found")
    })
    @GetMapping("/{carId}/images/main")
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

    @Operation(summary = "Get image by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image returned"),
        @ApiResponse(responseCode = "400", description = "Image does not belong to this car")
    })
    @GetMapping("/{carId}/images/{imageId}")
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

    @Operation(
            summary = "Get all images metadata",
            description = "Returns metadata for all images of the specified car.")
    @ApiResponse(responseCode = "200", description = "Image list returned")
    @GetMapping("/{carId}/images")
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

    @Operation(
            summary = "Set main image",
            description =
                    "Sets the specified image as the main image for the car. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Main image set"),
        @ApiResponse(
                responseCode = "400",
                description = "Image not found or does not belong to this car"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PatchMapping("/{carId}/images/{imageId}/main")
    public ResponseEntity<Void> setMainImage(@PathVariable Long carId, @PathVariable Long imageId) {

        carImageService.setMainImage(carId, imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete image",
            description =
                    "Deletes a specific image from the car. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Image deleted"),
        @ApiResponse(responseCode = "400", description = "Image not found in this car"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping("/{carId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long carId, @PathVariable Long imageId) {

        carImageService.deleteImage(carId, imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete all images",
            description =
                    "Deletes all images for the specified car. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "All images deleted"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping("/{carId}/images")
    public ResponseEntity<Void> deleteAllImages(@PathVariable Long carId) {
        carImageService.deleteAllCarImages(carId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get main images for all owner cars",
            description =
                    "Returns main image metadata for all cars belonging to the specified owner. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponse(responseCode = "200", description = "Owner main images returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/owners/{userId}/images/main")
    public ResponseEntity<List<OwnerMainImageResponse>> getOwnerMainImages(
            @PathVariable Long userId) {

        List<OwnerMainImageResponse> responses =
                carImageService.getMainImagesByUserId(userId).stream()
                        .map(
                                img ->
                                        new OwnerMainImageResponse(
                                                img.getCar().getId(),
                                                img.getId(),
                                                img.getFileName(),
                                                img.getContentType(),
                                                img.getFileSize(),
                                                img.isMain()))
                        .toList();

        return ResponseEntity.ok(responses);
    }
}
