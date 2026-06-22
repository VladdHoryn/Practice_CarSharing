package org.example.application;

import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
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
        car.setUserId(10L);
    }

    @Nested
    @DisplayName("createCar()")
    class CreateCarTests {

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
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
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
        void shouldThrowExceptionWhenCreatingCarWithNullBrand() {
            car.setBrand(null);
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
        void shouldThrowExceptionWhenCreatingCarWithNullModel() {
            car.setModel(null);
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
            verify(carRepository, never()).save(any(Car.class));
        }

        @Test
        void shouldThrowExceptionWhenCreatingCarWithZeroPrice() {
            car.setPricePerDay(0.0f);
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
            verify(carRepository, never()).save(any(Car.class));
        }

        @Test
        void shouldThrowExceptionWhenCreatingCarWithNegativePrice() {
            car.setPricePerDay(-50.0f);
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
            verify(carRepository, never()).save(any(Car.class));
        }

        @Test
        void shouldThrowExceptionWhenCreatingCarWithNullPrice() {
            car.setPricePerDay(null);
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.createCar(car));
            verify(carRepository, never()).save(any(Car.class));
        }
    }


    @Nested
    @DisplayName("getCarById()")
    class GetCarByIdTests {

        @Test
        void shouldFindCarByIdWhenExists() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            Car found = carApplicationService.getCarById(1L);
            assertThat(found.getId()).isEqualTo(1L);
            verify(carRepository, times(1)).findById(1L);
        }

        @Test
        void shouldThrowExceptionWhenCarNotFoundById() {
            when(carRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.getCarById(999L));
            verify(carRepository, times(1)).findById(999L);
        }
    }


    @Nested
    @DisplayName("getAllCars()")
    class GetAllCarsTests {

        @Test
        void shouldReturnAllCars() {
            when(carRepository.findAll()).thenReturn(Arrays.asList(car, new Car()));
            List<Car> result = carApplicationService.getAllCars();
            assertThat(result).hasSize(2);
        }

        @Test
        void shouldReturnEmptyListWhenNoCars() {
            when(carRepository.findAll()).thenReturn(Collections.emptyList());
            assertThat(carApplicationService.getAllCars()).isEmpty();
        }
    }


    @Nested
    @DisplayName("updateCar()")
    class UpdateCarTests {

        @Test
        void shouldUpdateCarSuccessfully() {
            Car update = new Car();
            update.setBrand("BMW");
            update.setModel("X5");
            update.setYear(2023);
            update.setCarClass(CarClass.BUSINESS);
            update.setPricePerDay(150.0f);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

            Car updated = carApplicationService.updateCar(1L, update);
            assertThat(updated.getBrand()).isEqualTo("BMW");
            assertThat(updated.getStatus()).isEqualTo(CarStatus.UNCONFIRMED);
        }

        @Test
        void shouldThrowWhenUpdatingRentedCar() {
            car.setStatus(CarStatus.RENTED);
            Car update = new Car();
            update.setBrand("BMW");
            update.setModel("X5");
            update.setYear(2023);
            update.setCarClass(CarClass.BUSINESS);
            update.setPricePerDay(100.0f);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.updateCar(1L, update));
            verify(carRepository, never()).save(any(Car.class));
        }

        @Test
        void shouldThrowWhenUpdatingWithBlankBrand() {
            Car update = new Car();
            update.setBrand("  ");
            update.setModel("X5");
            update.setYear(2023);
            update.setCarClass(CarClass.BUSINESS);
            update.setPricePerDay(100.0f);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.updateCar(1L, update));
        }
    }


    @Nested
    @DisplayName("deleteCar()")
    class DeleteCarTests {

        @Test
        void shouldDeleteCarSuccessfully() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            // Use any(Car.class) to resolve ambiguity with JpaSpecificationExecutor.delete(Specification)
            doNothing().when(carRepository).delete(any(Car.class));

            carApplicationService.deleteCar(1L);

            verify(carRepository).delete(any(Car.class));
        }

        @Test
        void shouldThrowWhenDeletingNonExistingCar() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.deleteCar(99L));
            verify(carRepository, never()).delete(any(Car.class));
        }
    }


    @Nested
    @DisplayName("getAvailableCars()")
    class GetAvailableCarsTests {

        @Test
        void shouldReturnAvailableCars() {
            when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(List.of(car));
            List<Car> result = carApplicationService.getAvailableCars();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        void shouldReturnEmptyListWhenNoCarsAvailable() {
            when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(Collections.emptyList());
            assertThat(carApplicationService.getAvailableCars()).isEmpty();
        }
    }


    @Nested
    @DisplayName("getUnconfirmedCars()")
    class GetUnconfirmedCarsTests {

        @Test
        void shouldReturnUnconfirmedCars() {
            car.setStatus(CarStatus.UNCONFIRMED);
            when(carRepository.findByStatus(CarStatus.UNCONFIRMED)).thenReturn(List.of(car));
            List<Car> result = carApplicationService.getUnconfirmedCars();
            assertThat(result).hasSize(1);
        }
    }


    @Nested
    @DisplayName("getCarsByUserId()")
    class GetCarsByUserIdTests {

        @Test
        void shouldReturnCarsForOwner() {
            when(carRepository.findCarByUserId(10L)).thenReturn(List.of(car));
            List<Car> result = carApplicationService.getCarsByUserId(10L);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(10L);
        }

        @Test
        void shouldReturnEmptyListWhenOwnerHasNoCars() {
            when(carRepository.findCarByUserId(99L)).thenReturn(Collections.emptyList());
            assertThat(carApplicationService.getCarsByUserId(99L)).isEmpty();
        }
    }


    @Nested
    @DisplayName("rentCar()")
    class RentCarTests {

        @Test
        void shouldRentAvailableCar() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            Car rented = carApplicationService.rentCar(1L, 5L);
            assertThat(rented.getStatus()).isEqualTo(CarStatus.RENTED);
            assertThat(rented.getUserId()).isEqualTo(5L);
        }

        @Test
        void shouldThrowWhenCarAlreadyRented() {
            car.setStatus(CarStatus.RENTED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.rentCar(1L, 5L));
            verify(carRepository, never()).save(any(Car.class));
        }

        @Test
        void shouldThrowWhenCarNotFound() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.rentCar(99L, 5L));
        }

        @Test
        void shouldThrowWhenUserIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.rentCar(1L, null));
        }

        @Test
        void shouldThrowWhenUserIdIsZero() {
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.rentCar(1L, 0L));
        }
    }


    @Nested
    @DisplayName("returnCar()")
    class ReturnCarTests {

        @Test
        void shouldReturnRentedCar() {
            car.setStatus(CarStatus.RENTED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            Car returned = carApplicationService.returnCar(1L);
            assertThat(returned.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        void shouldThrowWhenCarNotRented() {
            car.setStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.returnCar(1L));
        }

        @Test
        void shouldThrowWhenCarNotFound() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> carApplicationService.returnCar(99L));
        }
    }


    @Nested
    @DisplayName("sendToMaintenance()")
    class SendToMaintenanceTests {

        @Test
        void shouldSendAvailableCarToMaintenance() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            Car result = carApplicationService.sendToMaintenance(1L);
            assertThat(result.getStatus()).isEqualTo(CarStatus.MAINTENANCE);
        }

        @Test
        void shouldThrowWhenSendingRentedCarToMaintenance() {
            car.setStatus(CarStatus.RENTED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.sendToMaintenance(1L));
        }
    }


    @Nested
    @DisplayName("completeMaintenance()")
    class CompleteMaintenanceTests {

        @Test
        void shouldCompleteMaintenanceSuccessfully() {
            car.setStatus(CarStatus.MAINTENANCE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            Car result = carApplicationService.completeMaintenance(1L);
            assertThat(result.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        void shouldThrowWhenCarNotInMaintenance() {
            car.setStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.completeMaintenance(1L));
        }
    }


    @Nested
    @DisplayName("confirmCar()")
    class ConfirmCarTests {

        @Test
        void shouldConfirmUnconfirmedCar() {
            car.setStatus(CarStatus.UNCONFIRMED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            carApplicationService.confirmCar(1L);
            verify(carRepository).save(any(Car.class));
        }

        @Test
        void shouldThrowWhenConfirmingNonUnconfirmedCar() {
            car.setStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.confirmCar(1L));
        }
    }


    @Nested
    @DisplayName("cancelCar()")
    class CancelCarTests {

        @Test
        void shouldCancelUnconfirmedCar() {
            car.setStatus(CarStatus.UNCONFIRMED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
            carApplicationService.cancelCar(1L);
            verify(carRepository).save(any(Car.class));
        }

        @Test
        void shouldThrowWhenCancelingNonUnconfirmedCar() {
            car.setStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            assertThrows(IllegalStateException.class, () -> carApplicationService.cancelCar(1L));
        }
    }


    @Nested
    @DisplayName("countCarsByOwnerId()")
    class CountCarsByOwnerIdTests {

        @Test
        void shouldReturnCountForOwner() {
            when(carRepository.countByOwnerId(10L)).thenReturn(3L);
            assertThat(carApplicationService.countCarsByOwnerId(10L)).isEqualTo(3L);
        }

        @Test
        void shouldReturnZeroWhenOwnerHasNoCars() {
            when(carRepository.countByOwnerId(99L)).thenReturn(0L);
            assertThat(carApplicationService.countCarsByOwnerId(99L)).isZero();
        }

        @Test
        void shouldThrowWhenOwnerIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> carApplicationService.countCarsByOwnerId(null));
        }

        @Test
        void shouldThrowWhenOwnerIdIsZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> carApplicationService.countCarsByOwnerId(0L));
        }

        @Test
        void shouldThrowWhenOwnerIdIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> carApplicationService.countCarsByOwnerId(-1L));
        }
    }
}
