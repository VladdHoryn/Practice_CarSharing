package org.example.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/payment/v1/*/status/change")
                    .hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.POST, "/payment/v1")
                    .hasAnyRole("RENTER", "ADMINISTRATOR")
                .requestMatchers(HttpMethod.GET, "/payment/v1")
                    .hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.PUT, "/payment/v1/**")
                    .hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.DELETE, "/payment/v1/**")
                    .hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.PATCH, "/payment/v1/**")
                    .hasAnyRole("RENTER", "ADMINISTRATOR")
                .anyRequest().authenticated());
        return http.build();
    }
}
