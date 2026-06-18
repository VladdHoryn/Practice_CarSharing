package org.example.infrastructure.client;

import java.util.List;

import org.example.dto.CarDto;
import org.example.infrastructure.config.CarServiceProperties;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CarServiceClient {
    private final RestClient restClient;
    private final CarServiceProperties carServiceProperties;

    public CarServiceClient(
            RestClient.Builder restClientBuilder,
            CarServiceProperties carServiceProperties,
            RequestBodyService requestBodyBuilder) {
        this.carServiceProperties = carServiceProperties;

        this.restClient =
                restClientBuilder
                        .baseUrl(carServiceProperties.getCarPath())
                        .requestInterceptor(
                                (request, body, execution) -> {
                                    ServletRequestAttributes attributes =
                                            (ServletRequestAttributes)
                                                    RequestContextHolder.getRequestAttributes();

                                    if (attributes != null) {
                                        String authHeader =
                                                attributes.getRequest().getHeader("Authorization");
                                        if (authHeader != null) {
                                            request.getHeaders().add("Authorization", authHeader);
                                        }
                                    }
                                    return execution.execute(request, body);
                                })
                        .build();
    }

    public List<CarDto> getCarsByUserId(Long userId) {
        log.info("Request all cars that belong to user={}", userId);

        return restClient
                .get()
                .uri("/owner/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarDto>>() {});
    }
}
