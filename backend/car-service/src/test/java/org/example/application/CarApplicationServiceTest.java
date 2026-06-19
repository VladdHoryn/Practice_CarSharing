package org.example.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.example.domain.Car;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CarApplicationServiceTest {

    @Mock private CarRepository carRepository;

    @InjectMocks private CarApplicationService carApplicationService;

    @Test
    void shouldReturnCarsFromMockedRepository() {
        Car car = new Car();
        when(carRepository.findAll()).thenReturn(List.of(car));

        List<Car> result = carApplicationService.getAllCars();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnCarById() {
        Long carId = 1L;
        Car car = new Car();
        car.setId(carId);
        when(carRepository.findById(carId)).thenReturn(java.util.Optional.of(car));

        Car result = carApplicationService.getCarById(carId);

        assertNotNull(result);
        assertEquals(carId, result.getId());
        verify(carRepository, times(1)).findById(carId);
    }
}
