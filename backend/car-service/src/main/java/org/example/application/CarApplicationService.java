package org.example.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Car;
import org.example.domain.CarImage;
import org.example.domain.CarStatus;
import org.example.dto.*;
import org.example.repository.CarImageRepository;
import org.example.repository.CarRepository;
import org.example.specification.CarSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {

    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;


    // =====================================================
    // IMAGE OPERATIONS
    // =====================================================

    @Transactional
    public CarSummaryResponse uploadImage(Long carId, MultipartFile file) throws IOException {
        log.info("Uploading image for car id={}, file={}", carId, file.getOriginalFilename());

        // Валідація файлу
        validateImageFile(file);

        Car car = getCarById(carId);

        CarImage image = CarImage.builder()
                .car(car)
                .imageData(file.getBytes())
                .contentType(file.getContentType())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .isMain(car.getImages().isEmpty()) // перше фото стає головним
                .build();

        car.addImage(image);
        carRepository.save(car);

        return convertToSummaryResponse(car);
    }

    @Transactional
    public CarDetailedResponse setMainImage(Long carId, Long imageId) {
        log.info("Setting main image for car id={}, imageId={}", carId, imageId);

        Car car = getCarById(carId);
        car.setMainImage(imageId);
        carRepository.save(car);

        return convertToDetailedResponse(car);
    }

    @Transactional
    public CarDetailedResponse deleteImage(Long carId, Long imageId) {
        log.info("Deleting image id={} for car id={}", imageId, carId);

        Car car = getCarById(carId);
        boolean removed = car.removeImage(imageId);

        if (!removed) {
            throw new IllegalArgumentException("Image not found with id: " + imageId);
        }

        carRepository.save(car);
        return convertToDetailedResponse(car);
    }

    // =====================================================
    // CONVERTERS (для різних DTO)
    // =====================================================

    public CarSummaryResponse convertToSummaryResponse(Car car) {
        CarImage mainImage = car.getMainImage();

        return CarSummaryResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .carClass(car.getCarClass().name())
                .pricePerDay(car.getPricePerDay())
                .userId(car.getUserId())
                .status(car.getStatus())
                .locationCity(car.getLocationCity())
                .mainImage(mainImage != null ? mainImage.getImageData() : null)
                .build();
    }

    public CarDetailedResponse convertToDetailedResponse(Car car) {
        CarImage mainImage = car.getMainImage();

        List<byte[]> galleryImages = car.getImages().stream()
                .map(CarImage::getImageData)
                .toList();

        return CarDetailedResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .carClass(car.getCarClass().name())
                .pricePerDay(car.getPricePerDay())
                .userId(car.getUserId())
                .status(car.getStatus())
                .locationCity(car.getLocationCity())
                .mainImage(mainImage != null ? mainImage.getImageData() : null)
                .galleryImages(galleryImages)
                .build();
    }

    // =====================================================
    // FILTERING OPERATIONS
    // =====================================================

    public List<CarSummaryResponse> getAvailableCarsSummary() {
        log.debug("Fetching available cars (summary)");
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(this::convertToSummaryResponse)
                .toList();
    }

    public CarDetailedResponse getCarDetails(Long carId) {
        log.debug("Fetching car details for id={}", carId);
        Car car = getCarById(carId);
        return convertToDetailedResponse(car);
    }

    public Page<CarSummaryResponse> getFilteredCarsSummary(CarFilterDto filter, Pageable pageable) {
        log.debug("Filtering cars with criteria: {}", filter);
        return carRepository.findAll(CarSpecification.filterBy(filter), pageable)
                .map(this::convertToSummaryResponse);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateImageFile(MultipartFile file) {
        // Перевірка на пустий файл
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Перевірка типу файлу
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image (JPEG, PNG, etc.)");
        }

        // Перевірка розміру (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
    }

    // =====================================================
    // CRUD Operations
    // =====================================================

    @Transactional
    public Car createCar(Car car) {
        log.info("Creating new car: brand={}, model={}", car.getBrand(), car.getModel());

        if (car.getBrand() == null || car.getBrand().isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }
        if (car.getModel() == null || car.getModel().isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (car.getPricePerDay() == null || car.getPricePerDay() <= 0) {
            throw new IllegalArgumentException("Price per day must be positive");
        }

        car.setStatus(CarStatus.UNCONFIRMED);
        return carRepository.save(car);
    }

    @Transactional
    public Car updateCar(Long carId, Car updatedCar) {
        log.info("Updating car id={}", carId);

        Car existingCar =
                carRepository
                        .findById(carId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Car not found with id=" + carId));

        if (existingCar.getStatus().equals(CarStatus.RENTED)) {
            throw new IllegalArgumentException("Car is Rented for now");
        }

        if (updatedCar.getBrand() == null || updatedCar.getBrand().isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }

        if (updatedCar.getModel() == null || updatedCar.getModel().isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }

        if (updatedCar.getPricePerDay() == null || updatedCar.getPricePerDay() <= 0) {
            throw new IllegalArgumentException("Price per day must be positive");
        }

        existingCar.setBrand(updatedCar.getBrand());
        existingCar.setModel(updatedCar.getModel());
        existingCar.setYear(updatedCar.getYear());
        existingCar.setCarClass(updatedCar.getCarClass());
        existingCar.setPricePerDay(updatedCar.getPricePerDay());
        existingCar.setImageUrl(updatedCar.getImageUrl());

        existingCar.setStatus(CarStatus.UNCONFIRMED);

        log.info(
                "Car id={} updated successfully: brand={}, model={}",
                existingCar.getId(),
                existingCar.getBrand(),
                existingCar.getModel());

        return carRepository.save(existingCar);
    }

    public Car getCarById(Long id) {
        log.debug("Fetching car by id: {}", id);
        return carRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Car not found with id: " + id));
    }

    public List<Car> getAllCars() {
        log.debug("Fetching all cars");
        return carRepository.findAll();
    }

    @Transactional
    public void deleteCar(Long id) {
        log.info("Deleting car id={}", id);
        Car car = getCarById(id);
        carRepository.delete(car);
    }

    // =====================================================
    // Filtering Operations (from old CarService)
    // =====================================================

    public Page<Car> getFilteredCars(CarFilterDto filter, Pageable pageable) {
        log.debug("Filtering cars with criteria: {}", filter);
        return carRepository.findAll(CarSpecification.filterBy(filter), pageable);
    }

    public List<Car> getCarsByBrand(String brand) {
        log.debug("Fetching cars by brand: {}", brand);
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }
        return carRepository.findAll((root, query, cb) -> cb.equal(root.get("brand"), brand));
    }

    public List<Car> getAvailableCars() {
        log.debug("Fetching available cars");
        return carRepository.findByStatus(CarStatus.AVAILABLE);
    }

    public Boolean isAvailableById(Long id) {
        return carRepository.findById(id).get().isAvailableForRent();
    }

    public List<Car> getUnconfirmedCars() {
        log.debug("Fetching unconfirmed cars");
        return carRepository.findByStatus(CarStatus.UNCONFIRMED);
    }

    public List<Car> getCarsByUserId(Long userId) {
        log.debug("Fetching cars by userId: {}", userId);
        return carRepository.findCarByUserId(userId);
    }

    public List<Car> getCarsByClass(String carClass) {
        log.debug("Fetching cars by class: {}", carClass);
        if (carClass == null || carClass.isBlank()) {
            throw new IllegalArgumentException("Car class cannot be empty");
        }
        return carRepository.findAll((root, query, cb) -> cb.equal(root.get("carClass"), carClass));
    }

    // =====================================================
    // Business Logic Operations
    // =====================================================

    @Transactional
    public Car rentCar(Long carId, Long userId) {
        log.info("Rent request: carId={}, userId={}", carId, userId);

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }

        Car car = getCarById(carId);
        car.rent(userId);

        return carRepository.save(car);
    }

    @Transactional
    public Car returnCar(Long carId) {
        log.info("Return request: carId={}", carId);

        Car car = getCarById(carId);
        car.returnFromRent();

        return carRepository.save(car);
    }

    @Transactional
    public Car sendToMaintenance(Long carId) {
        log.info("Send to maintenance: carId={}", carId);

        Car car = getCarById(carId);
        car.sendToMaintenance();

        return carRepository.save(car);
    }

    @Transactional
    public Car completeMaintenance(Long carId) {
        log.info("Complete maintenance: carId={}", carId);

        Car car = getCarById(carId);
        car.completeMaintenance();

        return carRepository.save(car);
    }

    @Transactional
    public void changeStatus(Long carId, CarStatus newStatus) {
        Car car = getCarById(carId);

        car.changeStatus(newStatus);

        carRepository.save(car);
    }

    @Transactional
    public void confirmCar(Long carId) {
        Car car = getCarById(carId);

        car.confirmCar();

        carRepository.save(car);
    }

    @Transactional
    public void cancelCar(Long carId) {
        Car car = getCarById(carId);

        car.cancelCar();

        carRepository.save(car);
    }
    
    /**
     * Отримати всі фото авто (головне + галерея)
     */
    public List<String> getAllCarImages(Long carId) {
        Car car = getCarById(carId);
        return car.getAllImages();
    }

    /**
     * Отримати головне фото
     */
    public String getMainImage(Long carId) {
        Car car = getCarById(carId);
        if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
            return car.getImageUrl();
        }
        if (car.getPrimaryImage() != null && !car.getPrimaryImage().isEmpty()) {
            return car.getPrimaryImage();
        }
        if (car.getImages() != null && !car.getImages().isEmpty()) {
            return car.getImages().get(0);
        }
        return null;
    }
}
