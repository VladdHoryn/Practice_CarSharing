package org.example.application;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.example.domain.Car;
import org.example.domain.CarImage;
import org.example.exception.ImageNotProvidedException;
import org.example.repository.CarImageRepository;
import org.example.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarImageApplicationService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;

    private final CarApplicationService carApplicationService;

    @Transactional
    public void uploadImage(Long carId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Image upload failed for car id={}: file is null or empty", carId);
            throw new ImageNotProvidedException("Image file was not provided. Please upload image");
        }

        Car car =
                carRepository
                        .findById(carId)
                        .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        CarImage carImage =
                CarImage.builder()
                        .imageData(file.getBytes())
                        .contentType(file.getContentType())
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .build();

        car.addImage(carImage);

        carRepository.save(car);
        log.info("Uploaded new image for car id={}", carId);
    }

    @Transactional(readOnly = true)
    public List<CarImage> getMainImagesByUserId(Long userId) {
        log.info("Fetching main images for all cars owned by userId={}", userId);

        List<Car> userCars = carApplicationService.getCarsByUserId(userId);

        if (userCars == null || userCars.isEmpty()) {
            log.debug("User id={} has no cars", userId);
            return Collections.emptyList();
        }

        List<Long> carIds = userCars.stream().map(Car::getId).toList();

        return carImageRepository.findByCarIdInAndIsMainTrue(carIds);
    }

    @Transactional(readOnly = true)
    public CarImage getMainImage(Long carId) {
        return carImageRepository.findByCarIdAndIsMainTrue(carId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CarImage> getAllImages(Long carId) {
        return carImageRepository.findByCarId(carId);
    }

    @Transactional
    public void setMainImage(Long carId, Long imageId) {
        carImageRepository.clearMainFlagForCar(carId);

        CarImage newMainImage =
                carImageRepository
                        .findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (!newMainImage.getCar().getId().equals(carId)) {
            throw new IllegalArgumentException("Image does not belong to this car");
        }

        newMainImage.setMain(true);
        carImageRepository.save(newMainImage);
        log.info("Set image id={} as main for car id={}", imageId, carId);
    }

    @Transactional
    public void deleteImage(Long carId, Long imageId) {
        Car car =
                carRepository
                        .findById(carId)
                        .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        boolean removed = car.removeImage(imageId);

        if (!removed) {
            throw new IllegalArgumentException("Image not found in this car");
        }

        carRepository.save(car);
        log.info("Deleted image id={} from car id={}", imageId, carId);
    }

    @Transactional
    public void deleteAllCarImages(Long carId) {
        carImageRepository.deleteByCarId(carId);
        log.info("Deleted all images for car id={}", carId);
    }

    @Transactional(readOnly = true)
    public CarImage getImageById(Long imageId) {
        return carImageRepository
                .findById(imageId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Image not found with id=" + imageId));
    }
}
