package org.example.repository;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CarRepositoryTest {

  @Autowired
  private CarRepository carRepository;

  private Car saveCar(String brand, String model, CarStatus status, CarClass carClass) {
    Car car = new Car();
    car.setBrand(brand);
    car.setModel(model);
    car.setStatus(status);
    car.setCarClass(carClass);
    return carRepository.save(car);
  }

  @Test
  void shouldSaveCarToH2Database() {
    Car saved = saveCar("Porsche", "911", CarStatus.AVAILABLE, CarClass.BUSINESS);
    assertThat(saved.getId()).isNotNull();
  }

  @Test
  void shouldFindCarById() {
    Car saved = saveCar("Tesla", "Model 3", CarStatus.AVAILABLE, CarClass.BUSINESS);
    Optional<Car> found = carRepository.findById(saved.getId());
    assertThat(found).isPresent().contains(saved);
  }

  @Test
  void shouldReturnEmptyWhenCarDoesNotExist() {
    Optional<Car> found = carRepository.findById(9999L);
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindAllCarsInDatabase() {
    carRepository.deleteAll();
    saveCar("Kia", "Rio", CarStatus.AVAILABLE, CarClass.ECONOMY);
    saveCar("Mazda", "6", CarStatus.RENTED, CarClass.BUSINESS);

    List<Car> allCars = carRepository.findAll();
    assertThat(allCars).hasSize(2);
  }

  @Test
  void shouldDeleteCarById() {
    Car saved = saveCar("Renault", "Clio", CarStatus.AVAILABLE, CarClass.ECONOMY);
    carRepository.deleteById(saved.getId());

    Optional<Car> found = carRepository.findById(saved.getId());
    assertThat(found).isEmpty();
  }

  @Test
  void shouldUpdateCarFieldsSuccessfully() {
    Car saved = saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS);
    saved.setStatus(CarStatus.RENTED);
    saved.setModel("Camry Hybrid");

    Car updated = carRepository.save(saved);

    assertThat(updated.getStatus()).isEqualTo(CarStatus.RENTED);
    assertThat(updated.getModel()).isEqualTo("Camry Hybrid");
  }

  @Test
  void shouldVerifyCarExistsById() {
    Car saved = saveCar("Honda", "Civic", CarStatus.AVAILABLE, CarClass.ECONOMY);
    boolean exists = carRepository.existsById(saved.getId());
    assertThat(exists).isTrue();
  }

  @Test
  void shouldReturnFalseForNonExistingCarId() {
    boolean exists = carRepository.existsById(8888L);
    assertThat(exists).isFalse();
  }

  @Test
  void shouldCountTotalNumberOfCarsCorrectly() {
    carRepository.deleteAll();
    saveCar("Ford", "Mustang", CarStatus.AVAILABLE, CarClass.BUSINESS);
    saveCar("Ford", "Fiesta", CarStatus.RENTED, CarClass.ECONOMY);

    long count = carRepository.count();
    assertThat(count).isEqualTo(2);
  }

  @Test
  void shouldDeleteAllCarsFromRepository() {
    saveCar("Skoda", "Octavia", CarStatus.AVAILABLE, CarClass.BUSINESS);
    carRepository.deleteAll();

    assertThat(carRepository.count()).isZero();
  }
}
