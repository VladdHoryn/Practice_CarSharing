package org.example;

import java.util.Collections;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

@SpringBootApplication
public class ApiGatewayServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayServiceApplication.class, args);
  }

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
    corsConfig.setMaxAge(3600L);
    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    corsConfig.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
    corsConfig.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  @Bean
  public WebFilter corsDeduplicationFilter() {
    return (exchange, chain) -> {
      exchange.getResponse().beforeCommit(() -> {
        HttpHeaders headers = exchange.getResponse().getHeaders();

        // Перевіряємо заголовок Origin
        List<String> origins = headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        if (origins != null && origins.size() > 1) {
          String firstOrigin = origins.get(0);
          headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, firstOrigin);
        }

        // Перевіряємо заголовок Credentials
        List<String> credentials = headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        if (credentials != null && credentials.size() > 1) {
          String firstCredential = credentials.get(0);
          headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, firstCredential);
        }

        return reactor.core.publisher.Mono.empty();
      });
      return chain.filter(exchange);
    };
  }
}
