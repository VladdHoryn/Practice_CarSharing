package org.example.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.example.application.CarApplicationService;
import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.RentCarRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/car/v1")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CarController {

    private final CarApplicationService carService;

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

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(carService.getCarById(id)));
    }

    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars().stream().map(this::toResponse).toList());
    }

    @GetMapping("/unconfirmed")
    public ResponseEntity<List<CarResponse>> getAllUnconfirmedCars() {
        return ResponseEntity.ok(
                carService.getUnconfirmedCars().stream().map(this::toResponse).toList());
    }

    @GetMapping("/owner/{id}")
    public ResponseEntity<List<CarResponse>> getCarsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(
                carService.getCarsByUserId(id).stream().map(this::toResponse).toList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CarResponse>> getAvailableCars() {
        return ResponseEntity.ok(
                carService.getAvailableCars().stream().map(this::toResponse).toList());
    }

    @GetMapping("available/{id}")
    public Boolean isCarAvailable(@PathVariable Long id) {
        return carService.isAvailableById(id);
    }

    @PostMapping("/{carId}/rent")
    public ResponseEntity<CarResponse> rentCar(
            @PathVariable Long carId, @RequestBody @Valid RentCarRequest request) {
        return ResponseEntity.ok(toResponse(carService.rentCar(carId, request.userId())));
    }

    @PostMapping("/{carId}/return")
    public ResponseEntity<CarResponse> returnCar(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.returnCar(carId)));
    }

    @PostMapping("/{carId}/maintenance")
    public ResponseEntity<CarResponse> sendToMaintenance(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.sendToMaintenance(carId)));
    }

    @PostMapping("/{carId}/maintenance/complete")
    public ResponseEntity<CarResponse> completeMaintenance(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.completeMaintenance(carId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
