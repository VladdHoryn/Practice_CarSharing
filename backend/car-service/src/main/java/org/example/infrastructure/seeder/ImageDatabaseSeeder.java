package org.example.infrastructure.seeder;

import java.io.InputStream;

import org.example.application.CarImageApplicationService;
import org.example.repository.CarImageRepository;
import org.example.repository.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageDatabaseSeeder implements CommandLineRunner {

    private final CarImageApplicationService imageService;
    private final CarImageRepository carImageRepository;
    private final CarRepository carRepository;

    @Override
    public void run(String... args) {
        if (carImageRepository.count() > 0) {
            return;
        }

        log.info("Starting to seed car images...");

        String[] imageNames = {
            "toyota_yaris.jpg",
            "honda_fit.jpg",
            "toyota_camry.jpg",
            "honda_accord.jpg",
            "nissan_altima.jpg",
            "bmw_3series.jpg",
            "mercedes_cclass.jpg",
            "audi_a4.jpg",
            "bmw_x5.jpg",
            "mercedes_eclass.jpg",
            "audi_q7.jpg",
            "tesla_model3.jpg",
            "tesla_modely.jpg",
            "ford_focus.jpg",
            "hyundai_elantra.jpg",
            "kia_sportage.jpg",
            "lexus_rx350.jpg",
            "vw_passat.jpg"
        };

        for (int i = 0; i < imageNames.length; i++) {
            Long carId = (long) (i + 1);
            String fileName = imageNames[i];

            if (carRepository.existsById(carId)) {
                try {
                    ClassPathResource resource = new ClassPathResource("car_images/" + fileName);

                    try (InputStream is = resource.getInputStream()) {
                        MockMultipartFile file =
                                new MockMultipartFile("file", fileName, "image/jpeg", is);
                        imageService.uploadImage(carId, file);
                        log.info("Seeded image for car id={}", carId);
                    }
                } catch (Exception e) {
                    log.error(
                            "Failed to seed image {} for car {}: {}",
                            fileName,
                            carId,
                            e.getMessage());
                }
            }
        }
    }
}
