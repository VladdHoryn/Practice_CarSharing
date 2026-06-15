package org.example.application;

import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarApplicationServiceTest {

  @Mock
  private CarRepository carRepository;

  @InjectMocks
  private CarApplicationService carService;

  @Nested
  @DisplayName("Тести для методу createCar")
  class CreateCarTests {

    @Test
    @DisplayName("Успішне створення автомобіля зі статусом UNCONFIRMED")
    void shouldCreateCarSuccessfully() {
      Car inputCar = new Car();
      inputCar.setBrand("Tesla");
      inputCar.setModel("Model S");
      inputCar.setPricePerDay(150.0f);

      Car savedCar = new Car();
      savedCar.setId(1L);
      savedCar.setBrand("Tesla");
      savedCar.setModel("Model S");
      savedCar.setPricePerDay(150.0f);
      savedCar.setStatus(CarStatus.UNCONFIRMED);

      when(carRepository.save(any(Car.class))).thenReturn(savedCar);

      Car result = carService.createCar(inputCar);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getStatus()).isEqualTo(CarStatus.UNCONFIRMED);
      verify(carRepository, times(1)).save(inputCar);
    }

    @Test
    @DisplayName("Помилка, якщо бренд автомобіля пустий або null")
    void shouldThrowExceptionWhenBrandIsBlank() {
      Car carWithNullBrand = new Car();
      carWithNullBrand.setBrand(null);

      assertThatThrownBy(() -> carService.createCar(carWithNullBrand))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Brand is required");

      Car carWithEmptyBrand = new Car();
      carWithEmptyBrand.setBrand("   ");

      assertThatThrownBy(() -> carService.createCar(carWithEmptyBrand))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Brand is required");

      verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("Помилка, якщо модель автомобіля пуста або null")
    void shouldThrowExceptionWhenModelIsBlank() {
      Car car = new Car();
      car.setBrand("BMW");
      car.setModel("");

      assertThatThrownBy(() -> carService.createCar(car))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Model is required");
    }

    @Test
    @DisplayName("Помилка, якщо ціна за день оренди менша або дорівнює нулю")
    void shouldThrowExceptionWhenPriceIsInvalid() {
      Car car = new Car();
      car.setBrand("BMW");
      car.setModel("M5");
      car.setPricePerDay(0.0f);

      assertThatThrownBy(() -> carService.createCar(car))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Price per day must be positive");
    }
  }

  @Nested
  @DisplayName("Тести для методу updateCar")
  class UpdateCarTests {

    @Test
    @DisplayName("Помилка оновлення, якщо автомобіль зараз в оренді (RENTED)")
    void shouldThrowExceptionWhenUpdatingRentedCar() {
      Long carId = 1L;
      Car existingCar = new Car();
      existingCar.setId(carId);
      existingCar.setStatus(CarStatus.RENTED);

      Car updatedCar = new Car();

      when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));

      assertThatThrownBy(() -> carService.updateCar(carId, updatedCar))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Car is Rented for now");
    }

    @Test
    @DisplayName("Успішне оновлення характеристик автомобіля")
    void shouldUpdateCarSuccessfully() {
      Long carId = 1L;
      Car existingCar = new Car();
      existingCar.setId(carId);
      existingCar.setStatus(CarStatus.AVAILABLE);

      Car updatedCar = new Car();
      updatedCar.setBrand("Audi");
      updatedCar.setModel("A6");
      updatedCar.setPricePerDay(120.0f);

      when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
      when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

      Car result = carService.updateCar(carId, updatedCar);

      assertThat(result.getBrand()).isEqualTo("Audi");
      assertThat(result.getModel()).isEqualTo("A6");
      assertThat(result.getStatus()).isEqualTo(CarStatus.UNCONFIRMED);
    }
  }

  @Nested
  @DisplayName("Тести для читання та видалення (CRUD)")
  class ReadDeleteTests {

    @Test
    @DisplayName("Успішне отримання авто за ID")
    void shouldReturnCarById() {
      Long carId = 1L;
      Car car = new Car();
      car.setId(carId);

      when(carRepository.findById(carId)).thenReturn(Optional.of(car));

      Car result = carService.getCarById(carId);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(carId);
    }

    @Test
    @DisplayName("Помилка отримання авто, якщо ID не існує")
    void shouldThrowExceptionWhenCarNotFoundById() {
      Long carId = 999L;
      when(carRepository.findById(carId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> carService.getCarById(carId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Car not found with id: " + carId);
    }

    @Test
    @DisplayName("Успішне отримання списку всіх авто")
    void shouldReturnAllCars() {
      when(carRepository.findAll()).thenReturn(List.of(new Car(), new Car()));

      List<Car> result = carService.getAllCars();

      assertThat(result).hasSize(2);
      verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Успішне видалення авто за ID")
    void shouldDeleteCarSuccessfully() {
      Long carId = 1L;
      Car car = new Car();
      car.setId(carId);

      when(carRepository.findById(carId)).thenReturn(Optional.of(car));

      carService.deleteCar(carId);

      verify(carRepository, times(1)).delete(car);
    }
  }

  @Nested
  @DisplayName("Тести для фільтрації та списків")
  class FilteringTests {

    @Test
    @DisplayName("Отримання списку доступних машин")
    void shouldReturnAvailableCars() {
      when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(List.of(new Car()));

      List<Car> result = carService.getAvailableCars();

      assertThat(result).hasSize(1);
      verify(carRepository, times(1)).findByStatus(CarStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Отримання списку непідтверджених машин")
    void shouldReturnUnconfirmedCars() {
      when(carRepository.findByStatus(CarStatus.UNCONFIRMED)).thenReturn(List.of(new Car()));

      List<Car> result = carService.getUnconfirmedCars();

      assertThat(result).hasSize(1);
      verify(carRepository, times(1)).findByStatus(CarStatus.UNCONFIRMED);
    }

    @Test
    @DisplayName("Помилка пошуку за брендом, якщо рядок пустий")
    void shouldThrowExceptionWhenBrandIsEmpty() {
      assertThatThrownBy(() -> carService.getCarsByBrand(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Brand cannot be empty");
    }

    @Test
    @DisplayName("Помилка пошуку за класом авто, якщо рядок пустий")
    void shouldThrowExceptionWhenClassIsEmpty() {
      assertThatThrownBy(() -> carService.getCarsByClass("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Car class cannot be empty");
    }
  }

  @Nested
  @DisplayName("Тести для бізнес-операцій (Оренда та Сервіс)")
  class BusinessLogicTests {

    @Test
    @DisplayName("Успішна оренда автомобіля користувачем")
    void shouldRentCarSuccessfully() {
      Long carId = 1L;
      Long userId = 99L;

      Car carMock = mock(Car.class);

      when(carRepository.findById(carId)).thenReturn(Optional.of(carMock));
      when(carRepository.save(carMock)).thenReturn(carMock);

      carService.rentCar(carId, userId);

      verify(carMock, times(1)).rent(userId);
      verify(carRepository, times(1)).save(carMock);
    }

    @Test
    @DisplayName("Помилка оренди, якщо userId некоректний")
    void shouldThrowExceptionWhenUserIdIsInvalid() {
      assertThatThrownBy(() -> carService.rentCar(1L, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Valid user ID is required");

      assertThatThrownBy(() -> carService.rentCar(1L, -5L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Valid user ID is required");
    }

    @Test
    @DisplayName("Успішне повернення автомобіля з оренди")
    void shouldReturnCarFromRentSuccessfully() {
      Long carId = 1L;
      Car carMock = mock(Car.class);

      when(carRepository.findById(carId)).thenReturn(Optional.of(carMock));
      when(carRepository.save(carMock)).thenReturn(carMock);

      carService.returnCar(carId);

      verify(carMock, times(1)).returnFromRent();
      verify(carRepository, times(1)).save(carMock);
    }

    @Test
    @DisplayName("Успішне відправлення автомобіля на техобслуговування")
    void shouldSendToMaintenanceSuccessfully() {
      Long carId = 2L;
      Car carMock = mock(Car.class);

      when(carRepository.findById(carId)).thenReturn(Optional.of(carMock));
      when(carRepository.save(carMock)).thenReturn(carMock);

      carService.sendToMaintenance(carId);

      verify(carMock, times(1)).sendToMaintenance();
      verify(carRepository, times(1)).save(carMock);
    }

    @Test
    @DisplayName("Успішне завершення техобслуговування")
    void shouldCompleteMaintenanceSuccessfully() {
      Long carId = 3L;
      Car carMock = mock(Car.class);

      when(carRepository.findById(carId)).thenReturn(Optional.of(carMock));
      when(carRepository.save(carMock)).thenReturn(carMock);

      carService.completeMaintenance(carId);

      verify(carMock, times(1)).completeMaintenance();
      verify(carRepository, times(1)).save(carMock);
    }
  }
}
