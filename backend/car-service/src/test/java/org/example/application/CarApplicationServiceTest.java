package org.example.application;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarApplicationServiceTest {

  @Mock
  private CarRepository carRepository;

  @InjectMocks
  private CarApplicationService carApplicationService;

  private Car car;

  @BeforeEach
  void setUp() {
    car = new Car();
    car.setId(1L);
    car.setBrand("Audi");
    car.setModel("A6");
    car.setYear(2022);
    car.setStatus(CarStatus.AVAILABLE);
    car.setCarClass(CarClass.BUSINESS);
    car.setPricePerDay(100.0f);
  }

  // ---------- createCar ----------

  @Test
  void shouldCreateCarSuccessfully() {
    when(carRepository.save(any(Car.class))).thenReturn(car);

    Car created = carApplicationService.createCar(car);

    assertThat(created).isNotNull();
    assertThat(created.getBrand()).isEqualTo("Audi");
    verify(carRepository, times(1)).save(any(Car.class));
  }

  @Test
  void shouldSetStatusToUnconfirmedWhenCreatingCar() {
    when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Car created = carApplicationService.createCar(car);

    assertThat(created.getStatus()).isEqualTo(CarStatus.UNCONFIRMED);
  }

  @Test
  void shouldThrowExceptionWhenCreatingCarWithBlankBrand() {
    car.setBrand(" ");

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
    verify(carRepository, never()).save(any(Car.class));
  }

  @Test
  void shouldThrowExceptionWhenCreatingCarWithBlankModel() {
    car.setModel(" ");

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
    verify(carRepository, never()).save(any(Car.class));
  }

  @Test
  void shouldThrowExceptionWhenCreatingCarWithInvalidPrice() {
    car.setPricePerDay(0.0f);

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
    verify(carRepository, never()).save(any(Car.class));
  }

  // ---------- getCarById ----------

  @Test
  void shouldFindCarByIdWhenExists() {
    when(carRepository.findById(1L)).thenReturn(Optional.of(car));

    Car found = carApplicationService.getCarById(1L);

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(1L);
    verify(carRepository, times(1)).findById(1L);
  }

  @Test
  void shouldThrowExceptionWhenCarNotFoundById() {
    when(carRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.getCarById(999L));
    verify(carRepository, times(1)).findById(999L);
  }

  // ---------- getAllCars ----------

  @Test
  void shouldReturnAllCars() {
    List<Car> cars = Arrays.asList(car, new Car());
    when(carRepository.findAll()).thenReturn(cars);

    List<Car> result = carApplicationService.getAllCars();

    assertThat(result).hasSize(2);
    verify(carRepository, times(1)).findAll();
  }

  // ---------- updateCar ----------

  @Test
  void shouldUpdateCarSuccessfully() {
    Car updateRequest = new Car();
    updateRequest.setBrand("BMW");
    updateRequest.setModel("X5");
    updateRequest.setYear(2023);
    updateRequest.setCarClass(CarClass.BUSINESS);
    updateRequest.setPricePerDay(150.0f);

    when(carRepository.findById(1L)).thenReturn(Optional.of(car));
    when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Car updated = carApplicationService.updateCar(1L, updateRequest);

    assertThat(updated).isNotNull();
    assertThat(updated.getBrand()).isEqualTo("BMW");
    assertThat(updated.getStatus()).isEqualTo(CarStatus.UNCONFIRMED);
    verify(carRepository, times(1)).findById(1L);
    verify(carRepository, times(1)).save(any(Car.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistingCar() {
    when(carRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.updateCar(999L, car));
    verify(carRepository, times(1)).findById(999L);
    verify(carRepository, never()).save(any(Car.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingRentedCar() {
    car.setStatus(CarStatus.RENTED);
    when(carRepository.findById(1L)).thenReturn(Optional.of(car));

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.updateCar(1L, car));
    verify(carRepository, never()).save(any(Car.class));
  }

  // ---------- deleteCar ----------

  @Test
  void shouldDeleteCarSuccessfully() {
    when(carRepository.findById(1L)).thenReturn(Optional.of(car));
    doNothing().when(carRepository).delete(car);

    carApplicationService.deleteCar(1L);

    verify(carRepository, times(1)).findById(1L);
    verify(carRepository, times(1)).delete(car);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistingCar() {
    when(carRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.deleteCar(999L));
    verify(carRepository, never()).delete(any(Car.class));
  }

  // ---------- changeStatus (this replaces the old, non-existent "updateStatus") ----------
  // NOTE: changeStatus() is void in the real service and delegates to car.changeStatus(newStatus),
  // a domain method on Car whose internal rules I can't see without Car.java.
  // If Car.changeStatus() enforces transition rules (e.g. can't go RENTED -> RENTED),
  // adjust the starting status below accordingly.

  @Test
  void shouldChangeCarStatusSuccessfully() {
    when(carRepository.findById(1L)).thenReturn(Optional.of(car));
    when(carRepository.save(any(Car.class))).thenReturn(car);

    carApplicationService.changeStatus(1L, CarStatus.RENTED);

    verify(carRepository, times(1)).findById(1L);
    verify(carRepository, times(1)).save(car);
  }

  @Test
  void shouldThrowExceptionWhenChangingStatusOfNonExistingCar() {
    when(carRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> carApplicationService.changeStatus(999L, CarStatus.RENTED));
    verify(carRepository, never()).save(any(Car.class));
  }
}
