package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.application.CarApplicationService;
import org.example.config.SecurityConfig;
import org.example.domain.Car;
import org.example.domain.CarClass;
import org.example.domain.CarStatus;
import org.example.dto.CarStatusChange;
import org.example.dto.CreateCarRequest;
import org.example.dto.RentCarRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(controllers = CarController.class)
@Import(SecurityConfig.class)
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarApplicationService carService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Car car;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setYear(2022);
        car.setCarClass(CarClass.COMFORT);
        car.setPricePerDay(100.0f);
        car.setUserId(10L);
        car.setStatus(CarStatus.AVAILABLE);
    }

    private CreateCarRequest validCreateRequest() {
        return new CreateCarRequest("Toyota", "Camry", 2022, "COMFORT", 100.0f, 10L);
    }


    @Nested
    @DisplayName("GET /car/v1")
    class GetAllCars {

        @Test
        @WithMockUser
        @DisplayName("аутентифікований отримує список авто")
        void shouldReturnAllCarsForAuthenticatedUser() throws Exception {
            when(carService.getAllCars()).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].brand").value("Toyota"));
        }

        @Test
        @DisplayName("неавторизований отримує 4xx")
        void shouldReturn4xxForAnonymous() throws Exception {
            mockMvc.perform(get("/car/v1"))
                    .andExpect(status().is4xxClientError());
        }
    }


    @Nested
    @DisplayName("GET /car/v1/{id}")
    class GetCarById {

        @Test
        @WithMockUser
        @DisplayName("повертає 200 та авто за ID")
        void shouldReturnCarById() throws Exception {
            when(carService.getCarById(1L)).thenReturn(car);
            mockMvc.perform(get("/car/v1/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.brand").value("Toyota"));
        }

        @Test
        @WithMockUser
        @DisplayName("повертає 5xx якщо авто не знайдено")
        void shouldReturn5xxWhenCarNotFound() throws Exception {
            when(carService.getCarById(99L)).thenThrow(new RuntimeException("Car not found"));
            mockMvc.perform(get("/car/v1/99"))
                    .andExpect(status().is5xxServerError());
        }
    }


    @Nested
    @DisplayName("GET /car/v1/available")
    class GetAvailableCars {

        @Test
        @DisplayName("анонімний може отримати доступні авто (public endpoint)")
        void shouldReturnAvailableCarsForAnonymous() throws Exception {
            when(carService.getAvailableCars()).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1/available"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
        }

        @Test
        @WithMockUser
        @DisplayName("аутентифікований також отримує доступні авто")
        void shouldReturnAvailableCarsForAuthenticated() throws Exception {
            when(carService.getAvailableCars()).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1/available"))
                    .andExpect(status().isOk());
        }
    }


    @Nested
    @DisplayName("GET /car/v1/unconfirmed")
    class GetUnconfirmedCars {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує список непідтверджених авто")
        void shouldReturnUnconfirmedCarsForOwner() throws Exception {
            car.setStatus(CarStatus.UNCONFIRMED);
            when(carService.getUnconfirmedCars()).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1/unconfirmed"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR отримує список непідтверджених авто")
        void shouldReturnUnconfirmedCarsForAdmin() throws Exception {
            when(carService.getUnconfirmedCars()).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1/unconfirmed"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(get("/car/v1/unconfirmed"))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("GET /car/v1/owner/{id}")
    class GetCarsByOwnerId {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує авто за власником")
        void shouldReturnCarsByOwnerForOwner() throws Exception {
            when(carService.getCarsByUserId(10L)).thenReturn(List.of(car));
            mockMvc.perform(get("/car/v1/owner/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].userId").value(10));
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(get("/car/v1/owner/10"))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1")
    class CreateCar {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER може створити авто")
        void shouldCreateCarForOwner() throws Exception {
            when(carService.createCar(any())).thenReturn(car);
            mockMvc.perform(post("/car/v1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може створити авто")
        void shouldCreateCarForAdmin() throws Exception {
            when(carService.createCar(any())).thenReturn(car);
            mockMvc.perform(post("/car/v1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(post("/car/v1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("повертає 400 для некоректного запиту")
        void shouldReturn400ForInvalidRequest() throws Exception {
            String invalid = "{\"brand\":\"\",\"model\":\"\",\"year\":null,\"carClass\":null,\"pricePerDay\":null,\"userId\":null}";
            mockMvc.perform(post("/car/v1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalid))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("PUT /car/v1/{id}")
    class UpdateCar {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER може оновити авто")
        void shouldUpdateCarForOwner() throws Exception {
            when(carService.updateCar(eq(1L), any())).thenReturn(car);
            mockMvc.perform(put("/car/v1/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.brand").value("Toyota"));
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(put("/car/v1/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("DELETE /car/v1/{id}")
    class DeleteCar {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER може видалити авто")
        void shouldDeleteCarForOwner() throws Exception {
            doNothing().when(carService).deleteCar(1L);
            mockMvc.perform(delete("/car/v1/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може видалити авто")
        void shouldDeleteCarForAdmin() throws Exception {
            doNothing().when(carService).deleteCar(1L);
            mockMvc.perform(delete("/car/v1/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(delete("/car/v1/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/rent")
    class RentCar {

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER може орендувати авто")
        void shouldRentCarForRenter() throws Exception {
            car.setStatus(CarStatus.RENTED);
            when(carService.rentCar(eq(1L), anyLong())).thenReturn(car);
            mockMvc.perform(post("/car/v1/1/rent")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RentCarRequest(5L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RENTED"));
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може орендувати авто")
        void shouldRentCarForAdmin() throws Exception {
            car.setStatus(CarStatus.RENTED);
            when(carService.rentCar(eq(1L), anyLong())).thenReturn(car);
            mockMvc.perform(post("/car/v1/1/rent")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RentCarRequest(5L))))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує 403")
        void shouldReturn403ForOwner() throws Exception {
            mockMvc.perform(post("/car/v1/1/rent")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RentCarRequest(5L))))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/return")
    class ReturnCar {

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER може повернути авто")
        void shouldReturnCarForRenter() throws Exception {
            car.setStatus(CarStatus.AVAILABLE);
            when(carService.returnCar(1L)).thenReturn(car);
            mockMvc.perform(post("/car/v1/1/return").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("AVAILABLE"));
        }

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує 403")
        void shouldReturn403ForOwner() throws Exception {
            mockMvc.perform(post("/car/v1/1/return").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/maintenance")
    class SendToMaintenance {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER може відправити авто на обслуговування")
        void shouldSendToMaintenanceForOwner() throws Exception {
            car.setStatus(CarStatus.MAINTENANCE);
            when(carService.sendToMaintenance(1L)).thenReturn(car);
            mockMvc.perform(post("/car/v1/1/maintenance").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("MAINTENANCE"));
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(post("/car/v1/1/maintenance").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/maintenance/complete")
    class CompleteMaintenance {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER може завершити обслуговування")
        void shouldCompleteMaintenanceForOwner() throws Exception {
            car.setStatus(CarStatus.AVAILABLE);
            when(carService.completeMaintenance(1L)).thenReturn(car);
            mockMvc.perform(post("/car/v1/1/maintenance/complete").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR отримує 403 (endpoint тільки для OWNER)")
        void shouldReturn403ForAdmin() throws Exception {
            mockMvc.perform(post("/car/v1/1/maintenance/complete").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/moderation/confirm")
    class ConfirmCar {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може підтвердити авто")
        void shouldConfirmCarForAdmin() throws Exception {
            doNothing().when(carService).confirmCar(1L);
            mockMvc.perform(post("/car/v1/1/moderation/confirm").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує 403")
        void shouldReturn403ForOwner() throws Exception {
            mockMvc.perform(post("/car/v1/1/moderation/confirm").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(post("/car/v1/1/moderation/confirm").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/moderation/cancel")
    class CancelCar {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може скасувати авто")
        void shouldCancelCarForAdmin() throws Exception {
            doNothing().when(carService).cancelCar(1L);
            mockMvc.perform(post("/car/v1/1/moderation/cancel").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(post("/car/v1/1/moderation/cancel").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("POST /car/v1/{carId}/status/change")
    class ChangeStatus {

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR може змінити статус")
        void shouldChangeStatusForAdmin() throws Exception {
            doNothing().when(carService).changeStatus(anyLong(), any());
            CarStatusChange req = new CarStatusChange(CarStatus.MAINTENANCE);
            mockMvc.perform(post("/car/v1/1/status/change")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            CarStatusChange req = new CarStatusChange(CarStatus.AVAILABLE);
            mockMvc.perform(post("/car/v1/1/status/change")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("GET /car/v1/analytics/owners/{ownerId}/cars/count")
    class CountCarsByOwner {

        @Test
        @WithMockUser(roles = {"OWNER"})
        @DisplayName("OWNER отримує кількість своїх авто")
        void shouldCountCarsForOwner() throws Exception {
            when(carService.countCarsByOwnerId(10L)).thenReturn(3L);
            mockMvc.perform(get("/car/v1/analytics/owners/10/cars/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(3));
        }

        @Test
        @WithMockUser(roles = {"ADMINISTRATOR"})
        @DisplayName("ADMINISTRATOR отримує кількість авто власника")
        void shouldCountCarsForAdmin() throws Exception {
            when(carService.countCarsByOwnerId(10L)).thenReturn(5L);
            mockMvc.perform(get("/car/v1/analytics/owners/10/cars/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(5));
        }

        @Test
        @WithMockUser(roles = {"RENTER"})
        @DisplayName("RENTER отримує 403")
        void shouldReturn403ForRenter() throws Exception {
            mockMvc.perform(get("/car/v1/analytics/owners/10/cars/count"))
                    .andExpect(status().isForbidden());
        }
    }
}
