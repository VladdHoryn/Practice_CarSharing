package org.example.service;

import org.example.domain.Car;
import org.example.domain.CarImage;
import org.example.domain.CarStatus;
import org.example.repository.CarImageRepository;
import org.example.repository.CarRepository;
import org.example.application.CarApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarImageServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarImageRepository carImageRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CarApplicationService carService;

    private Car car;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setStatus(CarStatus.AVAILABLE);
    }

    @Test
    void uploadImage_ShouldSetAsMain_WhenFirstImage() throws IOException {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(carRepository.save(any(Car.class))).thenReturn(car);

        // When
        var response = carService.uploadImage(1L, multipartFile);

        // Then
        assertNotNull(response);
        assertEquals(1, car.getImages().size());
        assertTrue(car.getImages().get(0).isMain());
    }

    @Test
    void uploadImage_ShouldThrowException_WhenInvalidFileType() throws IOException {
        // Given
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.uploadImage(1L, multipartFile)
        );
        assertEquals("File must be an image (JPEG, PNG, etc.)", exception.getMessage());
    }

    @Test
    void deleteImage_ShouldSetNewMainImage_WhenDeletingMainImage() {
        // Given
        CarImage mainImage = CarImage.builder().id(1L).isMain(true).build();
        CarImage secondImage = CarImage.builder().id(2L).isMain(false).build();
        car.getImages().add(mainImage);
        car.getImages().add(secondImage);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);

        // When
        carService.deleteImage(1L, 1L);

        // Then
        assertFalse(mainImage.isMain());
        assertTrue(secondImage.isMain());
        assertEquals(1, car.getImages().size());
    }
}