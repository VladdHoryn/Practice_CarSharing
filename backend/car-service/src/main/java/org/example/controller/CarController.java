package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Car;
import org.example.dto.CarFilterDto;
import org.example.dto.CarImageDto;
import org.example.application.CarApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarApplicationService carService;

    // =====================================================
    // Основні CRUD операції
    // =====================================================

    @PostMapping
    public ResponseEntity<Car> createCar(@Valid @RequestBody Car car) {
        log.info("POST /api/cars - createCar");
        Car created = carService.createCar(car);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        log.info("GET /api/cars/{} - getCarById", id);
        Car car = carService.getCarById(id);
        return ResponseEntity.ok(car);
    }

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        log.info("GET /api/cars - getAllCars");
        return ResponseEntity.ok(carService.getAllCars());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        log.info("DELETE /api/cars/{} - deleteCar", id);
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // Операції з фото
    // =====================================================

    @PostMapping("/{id}/images")
    public ResponseEntity<Car> addImageToCar(@PathVariable Long id, @RequestBody String imageUrl) {
        log.info("POST /api/cars/{}/images - addImageToCar", id);
        Car car = carService.addImageToCar(id, imageUrl);
        return ResponseEntity.ok(car);
    }

    @DeleteMapping("/{id}/images/{index}")
    public ResponseEntity<Car> removeImageFromCar(@PathVariable Long id, @PathVariable int index) {
        log.info("DELETE /api/cars/{}/images/{} - removeImageFromCar", id, index);
        Car car = carService.removeImageFromCar(id, index);
        return ResponseEntity.ok(car);
    }

    @PutMapping("/{id}/primary-image")
    public ResponseEntity<Car> setPrimaryImage(@PathVariable Long id, @RequestBody String imageUrl) {
        log.info("PUT /api/cars/{}/primary-image - setPrimaryImage", id);
        Car car = carService.setPrimaryImage(id, imageUrl);
        return ResponseEntity.ok(car);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<CarImageDto> getCarImages(@PathVariable Long id) {
        log.info("GET /api/cars/{}/images - getCarImages", id);
        CarImageDto images = carService.getCarImages(id);
        return ResponseEntity.ok(images);
    }

    // =====================================================
    // Фільтрація
    // =====================================================

    @GetMapping("/filter")
    public ResponseEntity<Page<Car>> filterCars(CarFilterDto filter, Pageable pageable) {
        log.info("GET /api/cars/filter - filterCars");
        return ResponseEntity.ok(carService.getFilteredCars(filter, pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Car>> getAvailableCars() {
        log.info("GET /api/cars/available - getAvailableCars");
        return ResponseEntity.ok(carService.getAvailableCars());
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Car>> getCarsByBrand(@PathVariable String brand) {
        log.info("GET /api/cars/brand/{} - getCarsByBrand", brand);
        return ResponseEntity.ok(carService.getCarsByBrand(brand));
    }

    @GetMapping("/class/{carClass}")
    public ResponseEntity<List<Car>> getCarsByClass(@PathVariable String carClass) {
        log.info("GET /api/cars/class/{} - getCarsByClass", carClass);
        return ResponseEntity.ok(carService.getCarsByClass(carClass));
    }

    // =====================================================
    // Бізнес операції
    // =====================================================

    @PostMapping("/{id}/rent")
    public ResponseEntity<Car> rentCar(@PathVariable Long id, @RequestParam Long userId) {
        log.info("POST /api/cars/{}/rent - rentCar, userId={}", id, userId);
        Car car = carService.rentCar(id, userId);
        return ResponseEntity.ok(car);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Car> returnCar(@PathVariable Long id) {
        log.info("POST /api/cars/{}/return - returnCar", id);
        Car car = carService.returnCar(id);
        return ResponseEntity.ok(car);
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<Car> sendToMaintenance(@PathVariable Long id) {
        log.info("POST /api/cars/{}/maintenance - sendToMaintenance", id);
        Car car = carService.sendToMaintenance(id);
        return ResponseEntity.ok(car);
    }

    @PostMapping("/{id}/maintenance/complete")
    public ResponseEntity<Car> completeMaintenance(@PathVariable Long id) {
        log.info("POST /api/cars/{}/maintenance/complete - completeMaintenance", id);
        Car car = carService.completeMaintenance(id);
        return ResponseEntity.ok(car);
    }
}
