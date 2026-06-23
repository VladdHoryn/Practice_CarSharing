package org.example.application.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Year;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarTest {

    private Car car;
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        car = new Car();
        car.setId(1L);
        car.setBrand("Tesla");
        car.setModel("Model 3");
        car.setYear(2023);
        car.setCarClass(CarClass.ECONOMY);
        car.setStatus(CarStatus.AVAILABLE);
        car.setPricePerDay(120.0f);
        car.setUserId(100L);
    }

    @Test
    void shouldPassValidationWithAllValidFields() {
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenBrandIsEmptyOrTooShort() {
        car.setBrand("");
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        assertFalse(violations.isEmpty());

        car.setBrand("A");
        violations = validator.validate(car);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenModelIsEmptyOrTooLong() {
        car.setModel("");
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        assertFalse(violations.isEmpty());

        car.setModel("A".repeat(51));
        violations = validator.validate(car);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenYearIsBefore1950OrNull() {
        car.setYear(1949);
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        assertFalse(violations.isEmpty());

        car.setYear(null);
        violations = validator.validate(car);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenPriceIsZeroOrNegative() {
        car.setPricePerDay(0.0f);
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        assertFalse(violations.isEmpty());

        car.setPricePerDay(-15.5f);
        violations = validator.validate(car);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenYearIsCurrentOrPast() throws Exception {
        car.setYear(Year.now().getValue());
        var method = Car.class.getDeclaredMethod("validateYear");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(car));
    }

    @Test
    void shouldThrowExceptionWhenYearIsInFuture() throws Exception {
        car.setYear(Year.now().getValue() + 1);
        var method = Car.class.getDeclaredMethod("validateYear");
        method.setAccessible(true);

        java.lang.reflect.InvocationTargetException exception =
                assertThrows(
                        java.lang.reflect.InvocationTargetException.class,
                        () -> method.invoke(car));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
    }

    @Test
    void rent_ShouldRentCarSuccessfully() {
        car.setStatus(CarStatus.AVAILABLE);
        car.rent(42L);
        assertEquals(CarStatus.RENTED, car.getStatus());
        assertEquals(42L, car.getUserId());
    }

    @Test
    void rent_ShouldThrowException_WhenCarIsNotAvailable() {
        car.setStatus(CarStatus.MAINTENANCE);
        assertThrows(IllegalStateException.class, () -> car.rent(42L));
    }

    @Test
    void rent_ShouldThrowException_WhenRenterIdIsNull() {
        car.setStatus(CarStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class, () -> car.rent(null));
    }

    @Test
    void returnFromRent_ShouldReturnFromRentSuccessfully() {
        car.setStatus(CarStatus.RENTED);
        car.setUserId(42L);
        car.returnFromRent();
        assertEquals(CarStatus.AVAILABLE, car.getStatus());
        assertNull(car.getUserId());
    }

    @Test
    void returnFromRent_ShouldThrowException_WhenCarIsNotRented() {
        car.setStatus(CarStatus.AVAILABLE);
        assertThrows(IllegalStateException.class, car::returnFromRent);
    }

    @Test
    void sendToMaintenance_ShouldSendToMaintenanceSuccessfully() {
        car.setStatus(CarStatus.AVAILABLE);
        car.sendToMaintenance();
        assertEquals(CarStatus.MAINTENANCE, car.getStatus());
    }

    @Test
    void sendToMaintenance_ShouldThrowException_WhenCarIsRented() {
        car.setStatus(CarStatus.RENTED);
        assertThrows(IllegalStateException.class, car::sendToMaintenance);
    }

    @Test
    void completeMaintenance_ShouldCompleteMaintenanceSuccessfully() {
        car.setStatus(CarStatus.MAINTENANCE);
        car.completeMaintenance();
        assertEquals(CarStatus.AVAILABLE, car.getStatus());
    }

    @Test
    void completeMaintenance_ShouldThrowException_WhenCarIsNotInMaintenance() {
        car.setStatus(CarStatus.AVAILABLE);
        assertThrows(IllegalStateException.class, car::completeMaintenance);
    }

    @Test
    void isAvailableForRent_ShouldReturnTrueOrFalse_DependingOnStatus() {
        car.setStatus(CarStatus.AVAILABLE);
        assertTrue(car.isAvailableForRent());

        car.setStatus(CarStatus.RENTED);
        assertFalse(car.isAvailableForRent());
    }
}
