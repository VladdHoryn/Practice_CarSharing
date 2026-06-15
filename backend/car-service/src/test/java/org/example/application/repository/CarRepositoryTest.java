package org.example.application.repository;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CarRepositoryTest {

  @Autowired
  private CarRepository carRepository;

  @Test
  void shouldSaveAndFindCar() {
    Car car = new Car();
    car.setBrand("Audi");
    car.setModel("A6");
    car.setYear(2022);
    car.setCarClass(CarClass.BUSINESS);
    car.setStatus(CarStatus.AVAILABLE);
    car.setPricePerDay(200.0f);
    car.setUserId(1L);

    Car savedCar = carRepository.save(car);
    Car foundCar = carRepository.findById(savedCar.getId()).orElse(null);

    assertThat(foundCar).isNotNull();
    assertThat(foundCar.getBrand()).isEqualTo("Audi");
  }
}
