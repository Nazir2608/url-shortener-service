//package com.nazir.urlshortener.integration;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class HealthControllerIT extends AbstractIntegrationTest {
//
//    @Test
//    @DisplayName("GET /api/v1/health → 200 with status UP")
//    void healthEndpointReturnsUp() {
//        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/health", Map.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().get("status")).isEqualTo("UP");
//        assertThat(response.getBody().get("service")).isEqualTo("url-shortener-service");
//        assertThat(response.getBody()).containsKey("javaVersion");
//        assertThat(response.getBody()).containsKey("timestamp");
//    }
//
//    @Test
//    @DisplayName("GET /actuator/health → 200")
//    void actuatorHealthReturnsUp() {
//        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().get("status")).isEqualTo("UP");
//    }
//}
