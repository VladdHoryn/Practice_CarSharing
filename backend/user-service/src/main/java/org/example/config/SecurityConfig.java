package org.example.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
      .cors(Customizer.withDefaults())
      .sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(
        auth ->
          auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // 👑 ВИПРАВЛЕНО: Точний мапінг без букви "s" та зірочок для реєстрації
            .requestMatchers(HttpMethod.POST, "/user/v1").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())
      .oauth2ResourceServer(
        oauth2 ->
          oauth2.jwt(
            jwt ->
              jwt.jwtAuthenticationConverter(
                jwtAuthenticationConverter())));

    return http.build();
  }
  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri("http://keycloak:8080/realms/carsharing-realm/protocol/openid-connect/certs").build();
  }
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(HttpMethod.POST, "/user/v1");
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(
      jwt -> {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null && realmAccess.containsKey("roles")) {
          @SuppressWarnings("unchecked")
          List<String> roles = (List<String>) realmAccess.get("roles");

          for (String role : roles) {
            authorities.add(
              new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
          }
        }
        return authorities;
      });

    return converter;
  }
}
