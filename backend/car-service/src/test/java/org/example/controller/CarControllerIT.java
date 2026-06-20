package org.example.controller;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

// ВАЖЛИВО: для @Testcontainers/@Container потрібна окрема залежність,
// яка зараз відсутня в pom.xml (див. пояснення нижче).
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CarControllerIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void overrideDatasourceProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private CarRepository carRepository;

  @AfterEach
  void cleanUp() {
    carRepository.deleteAll();
  }

  @Test
  void shouldReturnOk_WhenFetchingCars() {
    Car car = new Car();
    car.setBrand("Audi");
    car.setModel("A6");
    car.setStatus(CarStatus.AVAILABLE);
    car.setCarClass(CarClass.BUSINESS);
    car.setPricePerDay(100.0f);
    carRepository.save(car);

    // ВАЖЛИВО: підтверди реальний шлях у CarController.java (як і в CarControllerTest)
    ResponseEntity<String> response = restTemplate.getForEntity("/car/v1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
