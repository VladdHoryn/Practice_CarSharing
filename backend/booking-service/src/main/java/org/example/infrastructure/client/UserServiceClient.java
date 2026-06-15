package org.example.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.infrastructure.config.UserServiceProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
      .requestInterceptor(((request, body, execution) -> {
        ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(attributes != null){
          String authHeader = attributes.getRequest().getHeader("Authorization");
          if(authHeader != null){
            request.getHeaders().add("Authorization", authHeader);
          }
        }
        return execution.execute(request, body);
      }))
      .build();
  }

  public Optional<Long> existByEmailAndDriverCode(String email, String driverCode){
    log.info("Requesting data weather user exist by email={} & driverCode={}", email, driverCode);

    Long userId = restClient.get()
      .uri("exist/driverCode?email={email}&driverCode={driverCode}",
        email, driverCode)
      .retrieve()
      .body(Long.class);

    return Optional.ofNullable(userId);
  }
}
