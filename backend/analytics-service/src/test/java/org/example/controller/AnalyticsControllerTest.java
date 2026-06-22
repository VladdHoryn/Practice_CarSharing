package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.example.application.AnalyticsAggregatorApplicationService;
import org.example.dto.AdminAnalyticsSummaryResponse;
import org.example.dto.OwnerAnalyticsSummaryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsControllerTest.NoOauthSecurityTestConfig.class)
class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AnalyticsAggregatorApplicationService analyticsAggregatorService;

    private static final String AUTH_HEADER = "Bearer test-token";

    @AfterEach
    void resetMock() {
        reset(analyticsAggregatorService);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class NoOauthSecurityTestConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analytics/owners/{ownerId}/summary")
    class GetOwnerSummary {

        private static final String URL = "/api/v1/analytics/owners/{ownerId}/summary";

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("OWNER отримує 200 і тіло відповіді")
        void returnsOk_whenCallerIsOwner() throws Exception {
            OwnerAnalyticsSummaryResponse response =
                    OwnerAnalyticsSummaryResponse.builder()
                            .totalCars(3L)
                            .totalBookings(10L)
                            .completedBookings(8L)
                            .totalRevenue(BigDecimal.valueOf(1500))
                            .monthlyRevenue(Collections.emptyList())
                            .weeklyLoad(Collections.emptyList())
                            .build();

            when(analyticsAggregatorService.getOwnerAnalyticsSummary(
                            eq(AUTH_HEADER), eq(42L), anyString(), anyList(), any(), any(), any()))
                    .thenReturn(response);

            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCars").value(3))
                    .andExpect(jsonPath("$.totalBookings").value(10))
                    .andExpect(jsonPath("$.completedBookings").value(8));
        }

        @Test
        @WithMockUser(roles = "ADMINISTRATOR")
        @DisplayName("ADMINISTRATOR теж може отримати owner summary")
        void returnsOk_whenCallerIsAdministrator() throws Exception {
            when(analyticsAggregatorService.getOwnerAnalyticsSummary(
                            anyString(), eq(42L), anyString(), anyList(), any(), any(), any()))
                    .thenReturn(OwnerAnalyticsSummaryResponse.builder().build());

            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "RENTER")
        @DisplayName("RENTER отримує 403 - немає доступу до owner summary")
        void returnsForbidden_whenCallerIsRenter() throws Exception {
            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("неавтентифікований користувач отримує 401/403")
        void rejectsAnonymousUser() throws Exception {
            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName(
                "дефолтні значення completedStatus і activeStatuses підставляються, якщо не"
                        + " передані")
        void appliesDefaultStatuses_whenNotProvided() throws Exception {
            when(analyticsAggregatorService.getOwnerAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(42L),
                            eq("COMPLETED"),
                            eq(List.of("ACTIVE", "IN_PROGRESS")),
                            any(),
                            any(),
                            any()))
                    .thenReturn(OwnerAnalyticsSummaryResponse.builder().build());

            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isOk());

            verify(analyticsAggregatorService)
                    .getOwnerAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(42L),
                            eq("COMPLETED"),
                            eq(List.of("ACTIVE", "IN_PROGRESS")),
                            any(),
                            any(),
                            any());
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("відсутній обов'язковий yearStart -> 400 Bad Request")
        void returnsBadRequest_whenRequiredDateParamMissing() throws Exception {
            mockMvc.perform(
                            get(URL, 42L)
                                    .header("Authorization", AUTH_HEADER)
                                    .param("weekStart", "2026-06-15T00:00:00")
                                    .param("weekEnd", "2026-06-21T23:59:00")
                                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analytics/admin/summary")
    class GetAdminSummary {

        private static final String URL = "/api/v1/analytics/admin/summary";

        @Test
        @WithMockUser(roles = "ADMINISTRATOR")
        @DisplayName("ADMINISTRATOR отримує 200 і тіло відповіді")
        void returnsOk_whenCallerIsAdministrator() throws Exception {
            AdminAnalyticsSummaryResponse response =
                    AdminAnalyticsSummaryResponse.builder()
                            .activeUsers(100L)
                            .totalOwners(30L)
                            .totalRenters(70L)
                            .totalBookings(500L)
                            .periodRevenue(BigDecimal.valueOf(98765))
                            .upcomingBookings(40L)
                            .monthlyRevenue(Collections.emptyList())
                            .bookingsByDayOfWeek(Collections.emptyList())
                            .build();

            when(analyticsAggregatorService.getAdminAnalyticsSummary(
                            eq(AUTH_HEADER),
                            anyList(),
                            anyString(),
                            anyList(),
                            any(),
                            any(),
                            any(),
                            any()))
                    .thenReturn(response);

            mockMvc.perform(get(URL).header("Authorization", AUTH_HEADER).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activeUsers").value(100))
                    .andExpect(jsonPath("$.totalOwners").value(30))
                    .andExpect(jsonPath("$.totalRenters").value(70));
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("OWNER отримує 403 - немає доступу до admin summary")
        void returnsForbidden_whenCallerIsOwner() throws Exception {
            mockMvc.perform(get(URL).header("Authorization", AUTH_HEADER).with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "RENTER")
        @DisplayName("RENTER отримує 403 - немає доступу до admin summary")
        void returnsForbidden_whenCallerIsRenter() throws Exception {
            mockMvc.perform(get(URL).header("Authorization", AUTH_HEADER).with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("неавтентифікований користувач отримує 401")
        void rejectsAnonymousUser() throws Exception {
            mockMvc.perform(get(URL).header("Authorization", AUTH_HEADER).with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMINISTRATOR")
        @DisplayName(
                "усі опціональні параметри відсутні -> контролер підставляє дефолтні дати і"
                        + " статуси")
        void appliesAllDefaults_whenOptionalParamsMissing() throws Exception {
            when(analyticsAggregatorService.getAdminAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELED")),
                            eq("COMPLETED"),
                            eq(List.of("CONFIRMED", "PENDING")),
                            any(),
                            any(),
                            any(),
                            any()))
                    .thenReturn(AdminAnalyticsSummaryResponse.builder().build());

            mockMvc.perform(get(URL).header("Authorization", AUTH_HEADER).with(csrf()))
                    .andExpect(status().isOk());

            verify(analyticsAggregatorService)
                    .getAdminAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELED")),
                            eq("COMPLETED"),
                            eq(List.of("CONFIRMED", "PENDING")),
                            any(),
                            any(),
                            any(),
                            any());
        }

        @Test
        @WithMockUser(roles = "ADMINISTRATOR")
        @DisplayName("явно передані дати/статуси перекривають дефолтні значення")
        void usesProvidedValues_whenExplicitlyPassed() throws Exception {
            when(analyticsAggregatorService.getAdminAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(List.of("COMPLETED")),
                            eq("COMPLETED"),
                            eq(List.of("ACTIVE")),
                            any(),
                            any(),
                            any(),
                            any()))
                    .thenReturn(AdminAnalyticsSummaryResponse.builder().build());

            mockMvc.perform(
                            get(URL).header("Authorization", AUTH_HEADER)
                                    .param("allStatuses", "COMPLETED")
                                    .param("completedStatus", "COMPLETED")
                                    .param("activeStatuses", "ACTIVE")
                                    .param("periodStart", "2026-05-01T00:00:00")
                                    .param("upcomingStart", "2026-06-01T00:00:00")
                                    .param("upcomingEnd", "2026-07-01T00:00:00")
                                    .param("yearStart", "2026-01-01T00:00:00")
                                    .with(csrf()))
                    .andExpect(status().isOk());

            verify(analyticsAggregatorService)
                    .getAdminAnalyticsSummary(
                            eq(AUTH_HEADER),
                            eq(List.of("COMPLETED")),
                            eq("COMPLETED"),
                            eq(List.of("ACTIVE")),
                            any(),
                            any(),
                            any(),
                            any());
        }
    }
}
