//package com.nazir.urlshortener.integration;
//
//import com.nazir.urlshortener.dto.request.CreateShortUrlRequest;
//import com.nazir.urlshortener.dto.response.ShortUrlResponse;
//import com.nazir.urlshortener.repository.ShortUrlRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//
//import java.net.URI;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("RedirectController Integration Tests")
//class RedirectControllerIT extends AbstractIntegrationTest {
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
//    @Nested
//    @DisplayName("GET /{slug} — Redirect")
//    class RedirectTests {
//
//        @Test
//        void shouldRedirectToOriginalUrl() {
//            // Create a short URL
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "redir-test", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Follow=false to capture 302 instead of following redirect
//            var noFollowTemplate = restTemplate.getRestTemplate();
//            // TestRestTemplate doesn't follow redirects by default, so this works
//
//            ResponseEntity<Void> response = restTemplate.exchange(
//                "/redir-test", HttpMethod.GET, null, Void.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
//            assertThat(response.getHeaders().getLocation())
//                .isEqualTo(URI.create("https://www.google.com"));
//        }
//
//        @Test
//        void shouldIncrementClickCount() {
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "click-count", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Click 3 times
//            for (int i = 0; i < 3; i++) {
//                restTemplate.exchange("/click-count", HttpMethod.GET, null, Void.class);
//            }
//
//            // Verify count
//            ResponseEntity<ShortUrlResponse> details = restTemplate.getForEntity(
//                "/api/v1/urls/click-count", ShortUrlResponse.class);
//
//            assertThat(details.getBody().clickCount()).isEqualTo(3);
//        }
//
//        @Test
//        void shouldReturn404ForUnknownSlug() {
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/no-such-slug", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        }
//
//        @Test
//        void shouldReturn410ForExpiredUrl() {
//            // Create URL that expires in the past (using repository directly)
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "expired", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Manually expire it
//            var entity = shortUrlRepository.findBySlug("expired").orElseThrow();
//            entity.setExpiresAt(LocalDateTime.now().minusDays(1));
//            shortUrlRepository.save(entity);
//
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/expired", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
//        }
//
//        @Test
//        void shouldReturn410ForDeactivatedUrl() {
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "inactive", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Deactivate
//            var entity = shortUrlRepository.findBySlug("inactive").orElseThrow();
//            entity.setActive(false);
//            shortUrlRepository.save(entity);
//
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/inactive", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /{slug}+ — Preview")
//    class PreviewTests {
//
//        @Test
//        void shouldReturnPreviewWithoutRedirect() {
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "preview-me", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            ResponseEntity<Map> response = restTemplate.getForEntity(
//                "/preview-me+", Map.class);
//
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().get("slug")).isEqualTo("preview-me");
//            assertThat(response.getBody().get("originalUrl")).isEqualTo("https://www.google.com");
//        }
//
//        @Test
//        void shouldNotCountClickOnPreview() {
//            var createReq = new CreateShortUrlRequest(
//                "https://www.google.com", "no-click", null, null, null, null);
//            restTemplate.postForEntity("/api/v1/urls", createReq, ShortUrlResponse.class);
//
//            // Preview 5 times
//            for (int i = 0; i < 5; i++) {
//                restTemplate.getForEntity("/no-click+", Map.class);
//            }
//
//            // Verify count is still 0
//            ResponseEntity<ShortUrlResponse> details = restTemplate.getForEntity(
//                "/api/v1/urls/no-click", ShortUrlResponse.class);
//
//            assertThat(details.getBody().clickCount()).isZero();
//        }
//    }
//}
