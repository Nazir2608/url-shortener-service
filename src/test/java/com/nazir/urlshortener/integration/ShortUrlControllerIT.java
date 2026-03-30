//package com.nazir.urlshortener.integration;
//import com.nazir.urlshortener.dto.request.BulkCreateRequest;
//import com.nazir.urlshortener.dto.request.CreateShortUrlRequest;
//import com.nazir.urlshortener.dto.request.UpdateShortUrlRequest;
//import com.nazir.urlshortener.dto.response.ShortUrlResponse;
//import com.nazir.urlshortener.repository.ShortUrlRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("ShortUrlController Integration Tests")
//class ShortUrlControllerIT extends AbstractIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private ShortUrlRepository shortUrlRepository;
//
//    @BeforeEach
//    void cleanUp() {
//        shortUrlRepository.deleteAll();
//    }
//
//    // ─── CREATE ──────────────────────────────────────────────
//
//    @Nested
//    @DisplayName("POST /api/v1/urls")
//    class CreateTests {
//
//        @Test
//        void shouldCreateShortUrlWithAutoSlug() {
//            var request = new CreateShortUrlRequest(
//                "https://www.google.com", null, null, null, null, null);
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().slug()).hasSize(7);
//            assertThat(response.getBody().originalUrl()).isEqualTo("https://www.google.com");
//            assertThat(response.getBody().shortUrl()).contains(response.getBody().slug());
//            assertThat(response.getBody().isActive()).isTrue();
//            assertThat(response.getBody().clickCount()).isZero();
//            assertThat(response.getHeaders().getLocation()).isNotNull();
//        }
//
//        @Test
//        void shouldCreateShortUrlWithCustomSlug() {
//            var request = new CreateShortUrlRequest(
//                "https://www.github.com", "my-repo", null, null, null, null);
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            assertThat(response.getBody().slug()).isEqualTo("my-repo");
//        }
//
//        @Test
//        void shouldRejectDuplicateCustomSlug() {
//            var request1 = new CreateShortUrlRequest(
//                "https://www.google.com", "unique1", null, null, null, null);
//            var request2 = new CreateShortUrlRequest(
//                "https://www.github.com", "unique1", null, null, null, null);
//
//            restTemplate.postForEntity("/api/v1/urls", request1, ShortUrlResponse.class);
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                "/api/v1/urls", request2, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
//        }
//
//        @Test
//        void shouldRejectBlankUrl() {
//            var request = new CreateShortUrlRequest(
//                "", null, null, null, null, null);
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        }
//
//        @Test
//        void shouldRejectReservedSlug() {
//            var request = new CreateShortUrlRequest(
//                "https://www.google.com", "admin", null, null, null, null);
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        }
//
//        @Test
//        void shouldAutoPrependsHttps() {
//            var request = new CreateShortUrlRequest(
//                "www.google.com", null, null, null, null, null);
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            assertThat(response.getBody().originalUrl()).startsWith("https://");
//        }
//
//        @Test
//        void shouldCreatePasswordProtectedUrl() {
//            var request = new CreateShortUrlRequest(
//                "https://secret.com", null, null, "mypassword", null, null);
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(
//                "/api/v1/urls", request, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            assertThat(response.getBody().isPasswordProtected()).isTrue();
//        }
//    }
//
//    // ─── READ ────────────────────────────────────────────────
//
//    @Nested
//    @DisplayName("GET /api/v1/urls/{slug}")
//    class GetTests {
//
//        @Test
//        void shouldGetUrlBySlug() {
//            // Create first
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "get-test", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Get
//            ResponseEntity<ShortUrlResponse> response = restTemplate.getForEntity(
//                "/api/v1/urls/get-test", ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody().slug()).isEqualTo("get-test");
//        }
//
//        @Test
//        void shouldReturn404ForUnknownSlug() {
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/api/v1/urls/nonexistent", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // ─── LIST ────────────────────────────────────────────────
//
//    @Nested
//    @DisplayName("GET /api/v1/urls")
//    class ListTests {
//
//        @Test
//        void shouldReturnPaginatedList() {
//            // Create 3 URLs
//            for (int i = 1; i <= 3; i++) {
//                var req = new CreateShortUrlRequest(
//                    "https://example.com/" + i, "list-" + i, null, null, null, null);
//                restTemplate.postForEntity("/api/v1/urls", req, ShortUrlResponse.class);
//            }
//
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/api/v1/urls?page=0&size=2", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            Map body = response.getBody();
//            assertThat(body).isNotNull();
//            assertThat((List<?>) body.get("content")).hasSize(2);
//            assertThat((int) body.get("totalElements")).isEqualTo(3);
//            assertThat((int) body.get("totalPages")).isEqualTo(2);
//        }
//    }
//
//    // ─── UPDATE ──────────────────────────────────────────────
//
//    @Nested
//    @DisplayName("PATCH /api/v1/urls/{slug}")
//    class UpdateTests {
//
//        @Test
//        void shouldUpdateOriginalUrl() {
//            // Create
//            var createReq = new CreateShortUrlRequest(
//                "https://old.com", "update-me", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Update
//            var updateReq = new UpdateShortUrlRequest(
//                "https://new.com", null, null, null);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<UpdateShortUrlRequest> entity = new HttpEntity<>(updateReq, headers);
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.exchange(
//                "/api/v1/urls/update-me", HttpMethod.PATCH, entity, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody().originalUrl()).isEqualTo("https://new.com");
//            assertThat(response.getBody().slug()).isEqualTo("update-me"); // slug unchanged
//        }
//
//        @Test
//        void shouldDeactivateUrl() {
//            var createReq = new CreateShortUrlRequest(
//                "https://example.com", "deactivate-me", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            var updateReq = new UpdateShortUrlRequest(null, false, null, null);
//            HttpEntity<UpdateShortUrlRequest> entity = new HttpEntity<>(updateReq, jsonHeaders());
//
//            ResponseEntity<ShortUrlResponse> response = restTemplate.exchange(
//                "/api/v1/urls/deactivate-me", HttpMethod.PATCH, entity, ShortUrlResponse.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody().isActive()).isFalse();
//        }
//    }
//
//    // ─── DELETE ──────────────────────────────────────────────
//
//    @Nested
//    @DisplayName("DELETE /api/v1/urls/{slug}")
//    class DeleteTests {
//
//        @Test
//        void shouldDeleteUrl() {
//            var createReq = new CreateShortUrlRequest(
//                "https://example.com", "delete-me", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            ResponseEntity<Void> response = restTemplate.exchange(
//                "/api/v1/urls/delete-me", HttpMethod.DELETE, null, Void.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
//            assertThat(shortUrlRepository.findBySlug("delete-me")).isEmpty();
//        }
//
//        @Test
//        void shouldReturn404WhenDeletingNonExistent() {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                "/api/v1/urls/no-such-slug", HttpMethod.DELETE, null, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // ─── BULK CREATE ─────────────────────────────────────────
//
//    @Nested
//    @DisplayName("POST /api/v1/urls/bulk")
//    class BulkCreateTests {
//
//        @Test
//        void shouldBulkCreateMultipleUrls() {
//            var bulkRequest = new BulkCreateRequest(List.of(
//                new CreateShortUrlRequest("https://a.com", "bulk-a", null, null, null, null),
//                new CreateShortUrlRequest("https://b.com", "bulk-b", null, null, null, null),
//                new CreateShortUrlRequest("https://c.com", null, null, null, null, null)
//            ));
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                "/api/v1/urls/bulk", bulkRequest, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            Map body = response.getBody();
//            assertThat((int) body.get("totalRequested")).isEqualTo(3);
//            assertThat((int) body.get("successCount")).isEqualTo(3);
//            assertThat((int) body.get("failureCount")).isZero();
//        }
//
//        @Test
//        void shouldHandlePartialFailuresInBulk() {
//            // Create first, then try to duplicate in bulk
//            var createReq = new CreateShortUrlRequest(
//                "https://exists.com", "existing", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            var bulkRequest = new BulkCreateRequest(List.of(
//                new CreateShortUrlRequest("https://new.com", "new-one", null, null, null, null),
//                new CreateShortUrlRequest("https://dup.com", "existing", null, null, null, null)
//            ));
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                "/api/v1/urls/bulk", bulkRequest, Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            Map body = response.getBody();
//            assertThat((int) body.get("successCount")).isEqualTo(1);
//            assertThat((int) body.get("failureCount")).isEqualTo(1);
//        }
//    }
//
//    // ─── HELPERS ─────────────────────────────────────────────
//
//    private HttpHeaders jsonHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//}
