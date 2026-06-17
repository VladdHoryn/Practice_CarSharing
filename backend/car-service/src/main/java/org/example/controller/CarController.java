package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Car;
import org.example.dto.*;
import org.example.application.CarApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;

import org.example.domain.CarClass;
import org.springframework.security.access.prepost.PreAuthorize;
import java.io.IOException;


@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarApplicationService carService;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    @PostMapping
    public ResponseEntity<Car> createCar(@Valid @RequestBody Car car) {
        log.info("POST /api/cars - createCar");
        Car created = carService.createCar(car);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CarSummaryResponse>> getAllCars() {
        log.info("GET /api/cars - getAllCars");
        return ResponseEntity.ok(carService.getAllCars().stream()
                .map(carService::convertToSummaryResponse)
                .toList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CarSummaryResponse>> getAvailableCars() {
        log.info("GET /api/cars/available - getAvailableCars (summary)");
        return ResponseEntity.ok(carService.getAvailableCarsSummary());
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CarSummaryResponse>> filterCars(CarFilterDto filter, Pageable pageable) {
        log.info("GET /api/cars/filter - filterCars");
        return ResponseEntity.ok(carService.getFilteredCarsSummary(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDetailedResponse> getCarById(@PathVariable Long id) {
        log.info("GET /api/cars/{} - getCarById (detailed)", id);
        return ResponseEntity.ok(carService.getCarDetails(id));
    }

    // =====================================================
    // IMAGE OPERATIONS
    // =====================================================

    @PostMapping(value = "/{carId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarSummaryResponse> uploadImage(
            @PathVariable Long carId,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("POST /api/cars/{}/images - uploadImage", carId);
        return ResponseEntity.ok(carService.uploadImage(carId, file));
    }

    @PutMapping("/{carId}/images/{imageId}/main")
    public ResponseEntity<CarDetailedResponse> setMainImage(
            @PathVariable Long carId,
            @PathVariable Long imageId) {
        log.info("PUT /api/cars/{}/images/{}/main - setMainImage", carId, imageId);
        return ResponseEntity.ok(carService.setMainImage(carId, imageId));
    }

    @DeleteMapping("/{carId}/images/{imageId}")
    public ResponseEntity<CarDetailedResponse> deleteImage(
            @PathVariable Long carId,
            @PathVariable Long imageId) {
        log.info("DELETE /api/cars/{}/images/{} - deleteImage", carId, imageId);
        return ResponseEntity.ok(carService.deleteImage(carId, imageId));
    }

    // =====================================================
    // BUSINESS OPERATIONS
    // =====================================================

    @PostMapping("/{id}/rent")
    public ResponseEntity<Car> rentCar(@PathVariable Long id, @RequestParam Long userId) {
        log.info("POST /api/cars/{}/rent - rentCar, userId={}", id, userId);
        Car car = carService.rentCar(id, userId);
        return ResponseEntity.ok(car);
    }

    private CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getCarClass().name(),
                car.getPricePerDay(),
                car.getUserId(),
                car.getStatus(),
                car.getImageUrl());
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody @Valid CreateCarRequest request) {
        Car car = new Car();
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setYear(request.year());
        car.setCarClass(CarClass.valueOf(request.carClass()));
        car.setPricePerDay(request.pricePerDay());
        car.setImageUrl(request.imageUrl());

        car.setUserId(request.userId());

        Car createdCar = carService.createCar(car);

        return ResponseEntity.created(URI.create("/car/v1/" + createdCar.getId()))
                .body(toResponse(createdCar));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable Long id, @RequestBody @Valid CreateCarRequest request) {

        Car car = new Car();
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setYear(request.year());
        car.setCarClass(CarClass.valueOf(request.carClass()));
        car.setPricePerDay(request.pricePerDay());
        car.setImageUrl(request.imageUrl());

        car.setUserId(request.userId());

        Car updatedCar = carService.updateCar(id, car);

        return ResponseEntity.ok(toResponse(updatedCar));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/unconfirmed")
    public ResponseEntity<List<CarResponse>> getAllUnconfirmedCars() {
        return ResponseEntity.ok(
                carService.getUnconfirmedCars().stream().map(this::toResponse).toList());
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/owner/{id}")
    public ResponseEntity<List<CarResponse>> getCarsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(
                carService.getCarsByUserId(id).stream().map(this::toResponse).toList());
    }

    @GetMapping("available/{id}")
    public Boolean isCarAvailable(@PathVariable Long id) {
        return carService.isAvailableById(id);
    }

    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PostMapping("/{carId}/rent")
    public ResponseEntity<CarResponse> rentCar(
            @PathVariable Long carId, @RequestBody @Valid RentCarRequest request) {
        return ResponseEntity.ok(toResponse(carService.rentCar(carId, request.userId())));
    }

    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/{id}/return")
    public ResponseEntity<Car> returnCar(@PathVariable Long id) {
        log.info("POST /api/cars/{}/return - returnCar", id);
        Car car = carService.returnCar(id);
        return ResponseEntity.ok(car);
    }

    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<Car> sendToMaintenance(@PathVariable Long id) {
        log.info("POST /api/cars/{}/maintenance - sendToMaintenance", id);
        Car car = carService.sendToMaintenance(id);
        return ResponseEntity.ok(car);
    }

    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/{id}/maintenance/complete")
    public ResponseEntity<Car> completeMaintenance(@PathVariable Long id) {
        log.info("POST /api/cars/{}/maintenance/complete - completeMaintenance", id);
        Car car = carService.completeMaintenance(id);
        return ResponseEntity.ok(car);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        log.info("DELETE /api/cars/{} - deleteCar", id);
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/status/change")
    public ResponseEntity<CarResponse> changeStatus(
            @PathVariable Long carId, @RequestBody CarStatusChange carStatusChange) {
        carService.changeStatus(carId, carStatusChange.newStatus());

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/moderation/confirm")
    public ResponseEntity<CarResponse> confirmCar(@PathVariable Long carId) {
        carService.confirmCar(carId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/moderation/cancel")
    public ResponseEntity<CarResponse> cancelCar(@PathVariable Long carId) {
        carService.cancelCar(carId);

        return ResponseEntity.noContent().build();
    }
}