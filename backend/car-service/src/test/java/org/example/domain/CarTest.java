package org.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

  private Car car;

  @BeforeEach
  void setUp() {
    car = new Car();
    car.setId(1L);
    car.setBrand("Audi");
    car.setModel("A6");
    car.setStatus(CarStatus.AVAILABLE);
    car.setCarClass(CarClass.BUSINESS);
  }

  @Test
  void shouldSetAndGetId() {
    car.setId(5L);
    assertEquals(5L, car.getId());
  }

  @Test
  void shouldSetAndGetBrand() {
    car.setBrand("BMW");
    assertEquals("BMW", car.getBrand());
  }

  @Test
  void shouldSetAndGetModel() {
    car.setModel("M5");
    assertEquals("M5", car.getModel());
  }

  @Test
  void shouldSetAndGetCarClass() {
    car.setCarClass(CarClass.ECONOMY);
    assertEquals(CarClass.ECONOMY, car.getCarClass());
  }

  @Test
  void shouldSetAndGetStatus() {
    car.setStatus(CarStatus.RENTED);
    assertEquals(CarStatus.RENTED, car.getStatus());
  }

  @ParameterizedTest
  @EnumSource(CarStatus.class)
  void shouldSupportAllPossibleStatuses(CarStatus status) {
    car.setStatus(status);
    assertEquals(status, car.getStatus());
  }

  @ParameterizedTest
  @EnumSource(CarClass.class)
  void shouldSupportAllPossibleCarClasses(CarClass carClass) {
    car.setCarClass(carClass);
    assertEquals(carClass, car.getCarClass());
  }

  @Test
  void entityShouldBeEqualToItself() {
    assertEquals(car, car);
  }

  @Test
  void entityShouldNotBeEqualToNull() {
    assertNotEquals(null, car);
  }

  @Test
  void toStringShouldExecuteWithoutExceptions() {
    String result = car.toString();
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
