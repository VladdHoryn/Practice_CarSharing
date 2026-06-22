package org.example.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.example.application.CarApplicationService;
import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.dto.CarResponse;
import org.example.dto.CarStatusChange;
import org.example.dto.CreateCarRequest;
import org.example.dto.RentCarRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/car/v1")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Car management endpoints")
@SecurityRequirement(name = "bearerAuth")
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
                car.getStatus());
    }

    @Operation(summary = "Create car", description = "Creates a new car listing. Set to UNCONFIRMED until approved. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Car created"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody @Valid CreateCarRequest request) {
        Car car = new Car();
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setYear(request.year());
        car.setCarClass(CarClass.valueOf(request.carClass()));
        car.setPricePerDay(request.pricePerDay());

        car.setUserId(request.userId());

        Car createdCar = carService.createCar(car);

        return ResponseEntity.created(URI.create("/car/v1/" + createdCar.getId()))
                .body(toResponse(createdCar));
    }

    @Operation(summary = "Update car", description = "Updates car details. Resets status to UNCONFIRMED. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Car updated"),
        @ApiResponse(responseCode = "400", description = "Car not found or currently rented"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
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

        car.setUserId(request.userId());

        Car updatedCar = carService.updateCar(id, car);

        return ResponseEntity.ok(toResponse(updatedCar));
    }

    @Operation(summary = "Get car by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Car found"),
        @ApiResponse(responseCode = "400", description = "Car not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(carService.getCarById(id)));
    }

    @Operation(summary = "Get all cars")
    @ApiResponse(responseCode = "200", description = "List of all cars returned")
    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars().stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Get all car IDs")
    @ApiResponse(responseCode = "200", description = "List of all car IDs returned")
    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getAllCarIds() {
        return ResponseEntity.ok(carService.getAllCars().stream().map(car -> car.getId()).toList());
    }

    @Operation(summary = "Get unconfirmed cars", description = "Returns cars pending moderation. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponse(responseCode = "200", description = "Unconfirmed cars returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/unconfirmed")
    public ResponseEntity<List<CarResponse>> getAllUnconfirmedCars() {
        return ResponseEntity.ok(
                carService.getUnconfirmedCars().stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Get cars by owner ID", description = "Returns all cars belonging to the specified owner. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponse(responseCode = "200", description = "Owner's cars returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/owner/{id}")
    public ResponseEntity<List<CarResponse>> getCarsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(
                carService.getCarsByUserId(id).stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Get available cars")
    @ApiResponse(responseCode = "200", description = "Available cars returned")
    @GetMapping("/available")
    public ResponseEntity<List<CarResponse>> getAvailableCars() {
        return ResponseEntity.ok(
                carService.getAvailableCars().stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Check if car is available")
    @ApiResponse(responseCode = "200", description = "Availability status returned")
    @GetMapping("available/{id}")
    public Boolean isCarAvailable(@PathVariable Long id) {
        return carService.isAvailableById(id);
    }

    @Operation(summary = "Rent a car", description = "Marks a car as rented by the given user. Accessible by RENTER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Car rented successfully"),
        @ApiResponse(responseCode = "400", description = "Car not available for rent"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('RENTER', 'ADMINISTRATOR')")
    @PostMapping("/{carId}/rent")
    public ResponseEntity<CarResponse> rentCar(
            @PathVariable Long carId, @RequestBody @Valid RentCarRequest request) {
        return ResponseEntity.ok(toResponse(carService.rentCar(carId, request.userId())));
    }

    @Operation(summary = "Return a car", description = "Marks a rented car as returned. Accessible by RENTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Car returned successfully"),
        @ApiResponse(responseCode = "400", description = "Car is not currently rented"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('RENTER')")
    @PostMapping("/{carId}/return")
    public ResponseEntity<CarResponse> returnCar(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.returnCar(carId)));
    }

    @Operation(summary = "Send car to maintenance", description = "Marks the car as under maintenance. Accessible by OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Car sent to maintenance"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/{carId}/maintenance")
    public ResponseEntity<CarResponse> sendToMaintenance(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.sendToMaintenance(carId)));
    }

    @Operation(summary = "Complete car maintenance", description = "Marks maintenance as complete. Accessible by OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Maintenance completed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping("/{carId}/maintenance/complete")
    public ResponseEntity<CarResponse> completeMaintenance(@PathVariable Long carId) {
        return ResponseEntity.ok(toResponse(carService.completeMaintenance(carId)));
    }

    @Operation(summary = "Delete car", description = "Deletes a car by ID. Accessible by OWNER or ADMINISTRATOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Car deleted"),
        @ApiResponse(responseCode = "400", description = "Car not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change car status (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status changed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/status/change")
    public ResponseEntity<CarResponse> changeStatus(
            @PathVariable Long carId, @RequestBody CarStatusChange carStatusChange) {
        carService.changeStatus(carId, carStatusChange.newStatus());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Confirm car (admin)", description = "Approves a car listing making it AVAILABLE.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Car confirmed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/moderation/confirm")
    public ResponseEntity<CarResponse> confirmCar(@PathVariable Long carId) {
        carService.confirmCar(carId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel car moderation (admin)", description = "Rejects a car listing.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Car rejected"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{carId}/moderation/cancel")
    public ResponseEntity<CarResponse> cancelCar(@PathVariable Long carId) {
        carService.cancelCar(carId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count cars by owner")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    @GetMapping("/analytics/owners/{ownerId}/cars/count")
    public ResponseEntity<Long> countCarsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(carService.countCarsByOwnerId(ownerId));
    }
}
