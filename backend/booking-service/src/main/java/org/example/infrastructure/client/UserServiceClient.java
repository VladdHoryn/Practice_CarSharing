package org.example.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.infrastructure.config.UserServiceProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
public class UserServiceClient {
  private final RestClient restClient;
  private final UserServiceProperties userServiceProperties;

  public UserServiceClient(RestClient.Builder restClientBuilder, UserServiceProperties userServiceProperties) {
    this.userServiceProperties = userServiceProperties;

    this.restClient = restClientBuilder
      .baseUrl(userServiceProperties.getUserPath())
      .build();
  }

  public Optional<Long> existByEmailAndDriverCode(String email, String driverCode){
    log.info("Requesting data weather user exist by email={} & driverCode={}", email, driverCode);

    Optional<Long> response = restClient.get()
      .uri(userServiceProperties.getUserPath() + "/exist/driverCode?email={email}&driverCode={driverCode}",
        email, driverCode)
      .retrieve()
      .body(new ParameterizedTypeReference<Optional<Long>>() {});

    return response;
  }
}
