package org.example.application;

import java.util.List;

import jakarta.transaction.Transactional;

import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.example.dto.CarFilterDto;
import org.example.repository.CarRepository;
import org.example.specification.CarSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {

    private final CarRepository carRepository;

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

    public long countCarsByOwnerId(Long ownerId) {
        log.debug("Counting cars for ownerId: {}", ownerId);

        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Valid owner ID is required");
        }

        return carRepository.countByOwnerId(ownerId);
    }
}
