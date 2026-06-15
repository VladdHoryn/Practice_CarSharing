package org.example.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.application.CarApplicationService;
import org.example.controller.CarController;
import org.example.domain.Car;
import org.example.domain.CarStatus;
import org.example.dto.CreateCarRequest;
import org.example.dto.RentCarRequest;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Новий імпорт для Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = CarController.class,
  excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CarControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CarApplicationService carService;

  @MockitoBean
  private CarRepository carRepository;

  private Car mockCar;
  private CreateCarRequest createCarRequest;

  @BeforeEach
  void setUp() {
    mockCar = new Car();
    mockCar.setId(1L);
    mockCar.setBrand("Tesla");
    mockCar.setModel("Model 3");
    mockCar.setYear(2023);

    // Визначаємо дефолтне ім'я класу машини з наявних в енумі
    String defaultCarClass = "ECONOMY";
    if (org.example.domain.CarClass.values().length > 0) {
      org.example.domain.CarClass firstClass = org.example.domain.CarClass.values()[0];
      mockCar.setCarClass(firstClass);
      defaultCarClass = firstClass.name();
    }

    mockCar.setPricePerDay(120.0f);
    mockCar.setUserId(42L);
    mockCar.setStatus(CarStatus.AVAILABLE);
    mockCar.setImageUrl("http://example.com/tesla.jpg");

    // Підставляємо РЕАЛЬНЕ ім'я константи в реквест, щоб мапер не падав
    createCarRequest = new CreateCarRequest(
      "Tesla",
      "Model 3",
      2023,
      defaultCarClass,
      120.0f,
      "http://example.com/tesla.jpg",
      42L
    );
  }

  @Test
  @DisplayName("POST /car/v1 — Успішне створення автомобіля")
  void shouldCreateCar() throws Exception {
    when(carService.createCar(any(Car.class))).thenReturn(mockCar);

    mockMvc.perform(post("/car/v1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createCarRequest)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "/car/v1/1"))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.brand").value("Tesla"))
      .andExpect(jsonPath("$.model").value("Model 3"))
      .andExpect(jsonPath("$.status").value("AVAILABLE"));

    verify(carService, times(1)).createCar(any(Car.class));
  }

  @Test
  @DisplayName("PUT /car/v1/{id} — Успішне оновлення автомобіля")
  void shouldUpdateCar() throws Exception {
    when(carService.updateCar(eq(1L), any(Car.class))).thenReturn(mockCar);

    mockMvc.perform(put("/car/v1/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createCarRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.brand").value("Tesla"));

    verify(carService, times(1)).updateCar(eq(1L), any(Car.class));
  }

  @Test
  @DisplayName("GET /car/v1/{id} — Отримання автомобіля за ID")
  void shouldGetCarById() throws Exception {
    when(carService.getCarById(1L)).thenReturn(mockCar);

    mockMvc.perform(get("/car/v1/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.brand").value("Tesla"));
  }

  @Test
  @DisplayName("GET /car/v1 — Отримання всіх автомобілів")
  void shouldGetAllCars() throws Exception {
    when(carService.getAllCars()).thenReturn(List.of(mockCar));

    mockMvc.perform(get("/car/v1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("size()").value(1));
  }

  @Test
  @DisplayName("GET /car/v1/unconfirmed — Отримання непідтверджених авто")
  void shouldGetAllUnconfirmedCars() throws Exception {
    mockCar.setStatus(CarStatus.UNCONFIRMED);
    when(carService.getUnconfirmedCars()).thenReturn(List.of(mockCar));

    mockMvc.perform(get("/car/v1/unconfirmed"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].status").value("UNCONFIRMED"));
  }

  @Test
  @DisplayName("GET /car/v1/owner/{id} — Отримання автомобілів власника")
  void shouldGetCarsByUserId() throws Exception {
    when(carService.getCarsByUserId(42L)).thenReturn(List.of(mockCar));

    mockMvc.perform(get("/car/v1/owner/42"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].userId").value(42));
  }

  @Test
  @DisplayName("GET /car/v1/available — Отримання доступних авто")
  void shouldGetAvailableCars() throws Exception {
    when(carService.getAvailableCars()).thenReturn(List.of(mockCar));

    mockMvc.perform(get("/car/v1/available"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
  }

  @Test
  @DisplayName("GET /car/v1/available/{id} — Перевірка доступності авто")
  void shouldCheckIfCarIsAvailable() throws Exception {
    when(carService.isAvailableById(1L)).thenReturn(true);

    mockMvc.perform(get("/car/v1/available/1"))
      .andExpect(status().isOk())
      .andExpect(content().string("true"));
  }

  @Test
  @DisplayName("POST /car/v1/{id}/rent — Успішна оренда авто")
  void shouldRentCar() throws Exception {
    RentCarRequest rentRequest = new RentCarRequest(99L);
    mockCar.setStatus(CarStatus.RENTED);
    mockCar.setUserId(99L);

    when(carService.rentCar(1L, 99L)).thenReturn(mockCar);

    mockMvc.perform(post("/car/v1/1/rent")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(rentRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("RENTED"))
      .andExpect(jsonPath("$.userId").value(99));
  }

  @Test
  @DisplayName("POST /car/v1/{id}/return — Повернення авто з оренди")
  void shouldReturnCar() throws Exception {
    when(carService.returnCar(1L)).thenReturn(mockCar);

    mockMvc.perform(post("/car/v1/1/return"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("AVAILABLE"));
  }

  @Test
  @DisplayName("POST /car/v1/{id}/maintenance — Відправка авто на сервіс")
  void shouldSendToMaintenance() throws Exception {
    mockCar.setStatus(CarStatus.MAINTENANCE);
    when(carService.sendToMaintenance(1L)).thenReturn(mockCar);

    mockMvc.perform(post("/car/v1/1/maintenance"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("MAINTENANCE"));
  }

  @Test
  @DisplayName("POST /car/v1/{id}/maintenance/complete — Завершення сервісу")
  void shouldCompleteMaintenance() throws Exception {
    when(carService.completeMaintenance(1L)).thenReturn(mockCar);

    mockMvc.perform(post("/car/v1/1/maintenance/complete"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("AVAILABLE"));
  }

  @Test
  @DisplayName("DELETE /car/v1/{id} — Видалення авто")
  void shouldDeleteCar() throws Exception {
    // ВИПРАВЛЕНО: Правильний синтаксис Mockito для void методів
    doNothing().when(carService).deleteCar(1L);

    mockMvc.perform(delete("/car/v1/1"))
      .andExpect(status().isNoContent());

    verify(carService, times(1)).deleteCar(1L);
  }
}
