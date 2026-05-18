package org.example.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.example.dto.CarFilterDto;
import org.example.dto.CarImageDto;
import org.example.repository.CarRepository;
import org.example.specification.CarSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {

    private final CarRepository carRepository;

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

        car.setStatus(CarStatus.AVAILABLE);
        return carRepository.save(car);
    }

    public Car getCarById(Long id) {
        log.debug("Fetching car by id: {}", id);
        return carRepository.findById(id)
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
    // Image Operations
    // =====================================================

    @Transactional
    public Car addImageToCar(Long carId, String imageUrl) {
        log.info("Adding image to car id={}", carId);
        
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        
        Car car = getCarById(carId);
        car.addImage(imageUrl);
        return carRepository.save(car);
    }

    @Transactional
    public Car removeImageFromCar(Long carId, int index) {
        log.info("Removing image at index {} from car id={}", index, carId);
        
        Car car = getCarById(carId);
        car.removeImage(index);
        return carRepository.save(car);
    }

    @Transactional
    public Car setPrimaryImage(Long carId, String imageUrl) {
        log.info("Setting primary image for car id={}", carId);
        
        Car car = getCarById(carId);
        car.setPrimaryImage(imageUrl);
        return carRepository.save(car);
    }

    public CarImageDto getCarImages(Long carId) {
        log.info("Getting images for car id={}", carId);
        
        Car car = getCarById(carId);
        CarImageDto dto = new CarImageDto();
        dto.setCarId(car.getId());
        dto.setImages(car.getImages());
        dto.setPrimaryImage(car.getPrimaryImage());
        return dto;
    }

    // =====================================================
    // Filtering Operations
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
        return carRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("brand"), brand));
    }

    public List<Car> getAvailableCars() {
        log.debug("Fetching available cars");
        return carRepository.findByStatus(CarStatus.AVAILABLE);
    }

    public List<Car> getCarsByClass(String carClass) {
        log.debug("Fetching cars by class: {}", carClass);
        if (carClass == null || carClass.isBlank()) {
            throw new IllegalArgumentException("Car class cannot be empty");
        }
        return carRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("carClass"), carClass));
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
}
