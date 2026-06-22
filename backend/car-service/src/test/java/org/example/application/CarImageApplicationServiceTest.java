package org.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarImage;
import org.example.domain.CarStatus;
import org.example.repository.CarImageRepository;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CarImageApplicationServiceTest {

    @Mock private CarRepository carRepository;

    @Mock private CarImageRepository carImageRepository;

    @InjectMocks private CarImageApplicationService carImageApplicationService;

    private Car car;
    private CarImage image;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setStatus(CarStatus.AVAILABLE);
        car.setCarClass(CarClass.BUSINESS);
        car.setPricePerDay(100.0f);
        car.setUserId(10L);

        image = new CarImage();
        image.setId(10L);
        image.setFileName("test.jpg");
        image.setContentType("image/jpeg");
        image.setImageData(new byte[] {1, 2, 3});
        image.setFileSize(3L);
        image.setMain(true);
        image.setCar(car);
    }

    @Nested
    @DisplayName("uploadImage()")
    class UploadImageTests {

        @Test
        @DisplayName("успішно завантажує зображення")
        void shouldUploadImageSuccessfully() throws IOException {
            MultipartFile file =
                    new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[] {1, 2, 3});
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any())).thenReturn(car);

            carImageApplicationService.uploadImage(1L, file);

            verify(carRepository).save(car);
            assertThat(car.getImages()).hasSize(1);
        }

        @Test
        @DisplayName("кидає виняток якщо авто не знайдено")
        void shouldThrowWhenCarNotFound() {
            MultipartFile file =
                    new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[] {1, 2, 3});
            when(carRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carImageApplicationService.uploadImage(99L, file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Car not found");
        }
    }

    @Nested
    @DisplayName("getMainImage()")
    class GetMainImageTests {

        @Test
        @DisplayName("повертає головне зображення")
        void shouldReturnMainImage() {
            when(carImageRepository.findByCarIdAndIsMainTrue(1L)).thenReturn(Optional.of(image));
            CarImage result = carImageApplicationService.getMainImage(1L);
            assertThat(result).isEqualTo(image);
        }

        @Test
        @DisplayName("повертає null якщо головного зображення немає")
        void shouldReturnNullWhenNoMainImage() {
            when(carImageRepository.findByCarIdAndIsMainTrue(1L)).thenReturn(Optional.empty());
            CarImage result = carImageApplicationService.getMainImage(1L);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getAllImages()")
    class GetAllImagesTests {

        @Test
        @DisplayName("повертає всі зображення авто")
        void shouldReturnAllImages() {
            when(carImageRepository.findByCarId(1L)).thenReturn(List.of(image));
            List<CarImage> result = carImageApplicationService.getAllImages(1L);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("повертає порожній список якщо немає зображень")
        void shouldReturnEmptyListWhenNoImages() {
            when(carImageRepository.findByCarId(1L)).thenReturn(List.of());
            List<CarImage> result = carImageApplicationService.getAllImages(1L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("setMainImage()")
    class SetMainImageTests {

        @Test
        @DisplayName("успішно призначає головне зображення")
        void shouldSetMainImageSuccessfully() {
            image.setCar(car);
            when(carImageRepository.findById(10L)).thenReturn(Optional.of(image));
            when(carImageRepository.save(any())).thenReturn(image);

            carImageApplicationService.setMainImage(1L, 10L);

            assertThat(image.isMain()).isTrue();
            verify(carImageRepository).save(image);
        }

        @Test
        @DisplayName("кидає виняток якщо зображення не знайдено")
        void shouldThrowWhenImageNotFound() {
            when(carImageRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> carImageApplicationService.setMainImage(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Image not found");
        }

        @Test
        @DisplayName("кидає виняток якщо зображення не належить даному авто")
        void shouldThrowWhenImageDoesNotBelongToCar() {
            Car otherCar = new Car();
            otherCar.setId(999L);
            image.setCar(otherCar);
            when(carImageRepository.findById(10L)).thenReturn(Optional.of(image));

            assertThatThrownBy(() -> carImageApplicationService.setMainImage(1L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong to this car");
        }
    }

    @Nested
    @DisplayName("deleteImage()")
    class DeleteImageTests {

        @Test
        @DisplayName("успішно видаляє зображення")
        void shouldDeleteImageSuccessfully() {
            car.addImage(image);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any())).thenReturn(car);

            carImageApplicationService.deleteImage(1L, image.getId());

            verify(carRepository).save(car);
        }

        @Test
        @DisplayName("кидає виняток якщо авто не знайдено")
        void shouldThrowWhenCarNotFound() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> carImageApplicationService.deleteImage(99L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Car not found");
        }
    }

    @Nested
    @DisplayName("deleteAllCarImages()")
    class DeleteAllCarImagesTests {

        @Test
        @DisplayName("успішно видаляє всі зображення авто")
        void shouldDeleteAllImages() {
            doNothing().when(carImageRepository).deleteByCarId(1L);
            carImageApplicationService.deleteAllCarImages(1L);
            verify(carImageRepository).deleteByCarId(1L);
        }
    }

    @Nested
    @DisplayName("getImageById()")
    class GetImageByIdTests {

        @Test
        @DisplayName("повертає зображення за ID")
        void shouldReturnImageById() {
            when(carImageRepository.findById(10L)).thenReturn(Optional.of(image));
            CarImage result = carImageApplicationService.getImageById(10L);
            assertThat(result).isEqualTo(image);
        }

        @Test
        @DisplayName("кидає виняток якщо зображення не знайдено")
        void shouldThrowWhenImageNotFound() {
            when(carImageRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> carImageApplicationService.getImageById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Image not found");
        }
    }
}
