package org.example.application;

import org.example.domain.Car;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CarApplicationServiceTest {

  @Mock
  private CarRepository carRepository; // Замокана база даних

  @InjectMocks
  private CarApplicationService carApplicationService; // Твій сервіс

  @Test
  void shouldReturnCarsFromMockedRepository() {
    // 1. Arrange: готуємо фейкову машину та кажемо репозиторію повернути її
    Car car = new Car();
    when(carRepository.findAll()).thenReturn(List.of(car));

    // 2. Act: викликаємо саме твій метод getAllCars()
    List<Car> result = carApplicationService.getAllCars();

    // 3. Assert: перевіряємо, що повернулася 1 машина
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(carRepository, times(1)).findAll(); // Перевіряємо, чи був запит до БД
  }
  @Test
  void shouldReturnCarById() {
    // Arrange
    Long carId = 1L;
    Car car = new Car();
    car.setId(carId);
    when(carRepository.findById(carId)).thenReturn(java.util.Optional.of(car));

    // Act
    Car result = carApplicationService.getCarById(carId);

    // Assert
    assertNotNull(result);
    assertEquals(carId, result.getId());
    verify(carRepository, times(1)).findById(carId);
  }
  @Test
  void shouldCreateCarSuccessfully() {
    // Arrange
    Car car = new Car();
    car.setBrand("Tesla");
    when(carRepository.save(any(Car.class))).thenReturn(car);

    // Act
    Car result = carApplicationService.createCar(car);

    // Assert
    assertNotNull(result);
    assertEquals("Tesla", result.getBrand());
    verify(carRepository, times(1)).save(car);
  }
}
