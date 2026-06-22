package org.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(
        properties = {
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.flyway.locations=classpath:db/migration-h2",
            "spring.sql.init.mode=never",
            "spring.jpa.database-platform=org.example.TestH2Dialect"
        })
class CarRepositoryTest {

    @Autowired private CarRepository carRepository;

    private Car saveCar(String brand, String model, CarStatus status, CarClass carClass) {
        return saveCar(brand, model, status, carClass, 1L);
    }

    private Car saveCar(
            String brand, String model, CarStatus status, CarClass carClass, Long userId) {
        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setStatus(status);
        car.setCarClass(carClass);
        car.setYear(2022);
        car.setPricePerDay(100.0f);
        car.setUserId(userId);
        return carRepository.save(car);
    }

    @BeforeEach
    void cleanUp() {
        carRepository.deleteAll();
    }

    @Nested
    @DisplayName("Базові CRUD операції")
    class BasicCrudTests {

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
            saveCar("Kia", "Rio", CarStatus.AVAILABLE, CarClass.ECONOMY);
            saveCar("Mazda", "6", CarStatus.RENTED, CarClass.BUSINESS);
            List<Car> allCars = carRepository.findAll();
            assertThat(allCars).hasSize(2);
        }

        @Test
        void shouldDeleteCarById() {
            Car saved = saveCar("Renault", "Clio", CarStatus.AVAILABLE, CarClass.ECONOMY);
            carRepository.deleteById(saved.getId());
            assertThat(carRepository.findById(saved.getId())).isEmpty();
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
            assertThat(carRepository.existsById(saved.getId())).isTrue();
        }

        @Test
        void shouldReturnFalseForNonExistingCarId() {
            assertThat(carRepository.existsById(8888L)).isFalse();
        }

        @Test
        void shouldCountTotalNumberOfCarsCorrectly() {
            saveCar("Ford", "Mustang", CarStatus.AVAILABLE, CarClass.BUSINESS);
            saveCar("Ford", "Fiesta", CarStatus.RENTED, CarClass.ECONOMY);
            assertThat(carRepository.count()).isEqualTo(2);
        }

        @Test
        void shouldDeleteAllCarsFromRepository() {
            saveCar("Skoda", "Octavia", CarStatus.AVAILABLE, CarClass.BUSINESS);
            carRepository.deleteAll();
            assertThat(carRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("findByStatus()")
    class FindByStatusTests {

        @Test
        @DisplayName("знаходить лише AVAILABLE авто")
        void shouldFindAvailableCars() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS);
            saveCar("BMW", "X5", CarStatus.RENTED, CarClass.BUSINESS);
            saveCar("Audi", "A4", CarStatus.MAINTENANCE, CarClass.BUSINESS);

            List<Car> available = carRepository.findByStatus(CarStatus.AVAILABLE);
            assertThat(available).hasSize(1);
            assertThat(available.get(0).getBrand()).isEqualTo("Toyota");
        }

        @Test
        @DisplayName("знаходить лише RENTED авто")
        void shouldFindRentedCars() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS);
            saveCar("BMW", "X5", CarStatus.RENTED, CarClass.BUSINESS);

            List<Car> rented = carRepository.findByStatus(CarStatus.RENTED);
            assertThat(rented).hasSize(1);
            assertThat(rented.get(0).getBrand()).isEqualTo("BMW");
        }

        @Test
        @DisplayName("повертає порожній список якщо немає авто з таким статусом")
        void shouldReturnEmptyWhenNoMatch() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS);

            List<Car> maintenance = carRepository.findByStatus(CarStatus.MAINTENANCE);
            assertThat(maintenance).isEmpty();
        }

        @Test
        @DisplayName("знаходить кілька авто з однаковим статусом")
        void shouldFindMultipleCarsWithSameStatus() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS);
            saveCar("Honda", "Civic", CarStatus.AVAILABLE, CarClass.ECONOMY);
            saveCar("BMW", "X5", CarStatus.RENTED, CarClass.BUSINESS);

            List<Car> available = carRepository.findByStatus(CarStatus.AVAILABLE);
            assertThat(available).hasSize(2);
        }

        @Test
        @DisplayName("знаходить UNCONFIRMED авто")
        void shouldFindUnconfirmedCars() {
            saveCar("Mazda", "6", CarStatus.UNCONFIRMED, CarClass.BUSINESS);
            saveCar("Kia", "Rio", CarStatus.AVAILABLE, CarClass.ECONOMY);

            List<Car> unconfirmed = carRepository.findByStatus(CarStatus.UNCONFIRMED);
            assertThat(unconfirmed).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findCarByUserId()")
    class FindCarByUserIdTests {

        @Test
        @DisplayName("знаходить авто за userId")
        void shouldFindCarsByUserId() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 100L);
            saveCar("Honda", "Civic", CarStatus.AVAILABLE, CarClass.ECONOMY, 100L);
            saveCar("BMW", "X5", CarStatus.AVAILABLE, CarClass.BUSINESS, 200L);

            List<Car> userCars = carRepository.findCarByUserId(100L);
            assertThat(userCars).hasSize(2);
        }

        @Test
        @DisplayName("повертає порожній список для userId без авто")
        void shouldReturnEmptyForUserWithNoCars() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 100L);

            List<Car> userCars = carRepository.findCarByUserId(999L);
            assertThat(userCars).isEmpty();
        }

        @Test
        @DisplayName("знаходить лише авто конкретного власника")
        void shouldFindOnlySpecificOwnerCars() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 10L);
            saveCar("BMW", "X5", CarStatus.RENTED, CarClass.BUSINESS, 20L);

            List<Car> owner10Cars = carRepository.findCarByUserId(10L);
            assertThat(owner10Cars).hasSize(1);
            assertThat(owner10Cars.get(0).getBrand()).isEqualTo("Toyota");
        }
    }

    @Nested
    @DisplayName("countByOwnerId()")
    class CountByOwnerIdTests {

        @Test
        @DisplayName("підраховує авто власника коректно")
        void shouldCountCarsByOwnerId() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 50L);
            saveCar("Honda", "Civic", CarStatus.AVAILABLE, CarClass.ECONOMY, 50L);
            saveCar("BMW", "X5", CarStatus.RENTED, CarClass.BUSINESS, 50L);
            saveCar("Audi", "A4", CarStatus.AVAILABLE, CarClass.BUSINESS, 99L);

            long count = carRepository.countByOwnerId(50L);
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("повертає 0 якщо у власника немає авто")
        void shouldReturnZeroForOwnerWithNoCars() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 50L);

            long count = carRepository.countByOwnerId(999L);
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("повертає 1 для власника з одним авто")
        void shouldReturnOneForSingleCarOwner() {
            saveCar("Toyota", "Camry", CarStatus.AVAILABLE, CarClass.BUSINESS, 77L);

            long count = carRepository.countByOwnerId(77L);
            assertThat(count).isEqualTo(1L);
        }
    }
}
