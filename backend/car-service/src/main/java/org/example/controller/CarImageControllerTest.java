package org.example.controller;

import org.example.application.CarApplicationService;
import org.example.dto.CarDetailedResponse;
import org.example.dto.CarSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarController.class)
class CarImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarApplicationService carService;

    @Test
    void getAvailableCars_ShouldReturnSummaryWithoutGalleryImages() throws Exception {
        // Given
        CarSummaryResponse response = CarSummaryResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .mainImage(new byte[]{1, 2, 3})
                .build();

        when(carService.getAvailableCarsSummary()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/cars/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[0].mainImage").exists())
                .andExpect(jsonPath("$[0].galleryImages").doesNotExist());
    }

    @Test
    void uploadImage_ShouldReturnOk() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        CarSummaryResponse response = CarSummaryResponse.builder()
                .id(1L)
                .brand("Toyota")
                .build();

        when(carService.uploadImage(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/cars/1/images")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}