package org.example.controller;

import jakarta.validation.Valid;
import lombok.*;
import org.example.application.CarApplicationService;
import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.RentCarRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

  private final CarApplicationService carService;

  // 🔹 Mapper (тимчасово тут)
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
      car.getImageUrl()
    );
  }

  // CREATE
  @PostMapping
  public ResponseEntity<CarResponse> createCar(@RequestBody @Valid CreateCarRequest request) {

    Car car = new Car();
    car.setBrand(request.brand());
    car.setModel(request.model());
    car.setYear(request.year());
    car.setCarClass(CarClass.valueOf(request.carClass()));
    car.setPricePerDay(request.pricePerDay());
    car.setImageUrl(request.imageUrl());

    Car createdCar = carService.createCar(car);

    return ResponseEntity.ok(toResponse(createdCar));
  }

  // READ BY ID
  @GetMapping("/{id}")
  public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
    return ResponseEntity.ok(toResponse(carService.getCarById(id)));
  }

  // READ ALL
  @GetMapping
  public ResponseEntity<List<CarResponse>> getAllCars() {
    return ResponseEntity.ok(
      carService.getAllCars()
        .stream()
        .map(this::toResponse)
        .toList()
    );
  }

  // GET AVAILABLE
  @GetMapping("/available")
  public ResponseEntity<List<CarResponse>> getAvailableCars() {
    return ResponseEntity.ok(
      carService.getAvailableCars()
        .stream()
        .map(this::toResponse)
        .toList()
    );
  }

  // RENT
  @PostMapping("/{carId}/rent")
  public ResponseEntity<CarResponse> rentCar(
    @PathVariable Long carId,
    @RequestBody @Valid RentCarRequest request
  ) {
    return ResponseEntity.ok(
      toResponse(carService.rentCar(carId, request.userId()))
    );
  }

  // RETURN
  @PostMapping("/{carId}/return")
  public ResponseEntity<CarResponse> returnCar(@PathVariable Long carId) {
    return ResponseEntity.ok(
      toResponse(carService.returnCar(carId))
    );
  }

  // SEND TO MAINTENANCE
  @PostMapping("/{carId}/maintenance")
  public ResponseEntity<CarResponse> sendToMaintenance(@PathVariable Long carId) {
    return ResponseEntity.ok(
      toResponse(carService.sendToMaintenance(carId))
    );
  }

  // COMPLETE MAINTENANCE
  @PostMapping("/{carId}/maintenance/complete")
  public ResponseEntity<CarResponse> completeMaintenance(@PathVariable Long carId) {
    return ResponseEntity.ok(
      toResponse(carService.completeMaintenance(carId))
    );
  }

  // DELETE
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
    carService.deleteCar(id);
    return ResponseEntity.noContent().build();
  }
}
