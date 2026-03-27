package com.nazir.urlshortener.unit.service;

import com.nazir.urlshortener.exception.InvalidUrlException;
import com.nazir.urlshortener.service.UrlValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UrlValidationService")
class UrlValidationServiceTest {

    private UrlValidationService service;

    @BeforeEach
    void setUp() {
        service = new UrlValidationService();
    }

    @Nested
    @DisplayName("valid URLs")
    class ValidUrls {

        @ParameterizedTest
        @ValueSource(strings = {
            "https://www.google.com",
            "http://example.com/path?query=1",
            "https://sub.domain.co.uk/path/to/resource#section",
            "https://example.com:8080/api"
        })
        void shouldAcceptValidUrls(String url) {
            String result = service.validateAndNormalize(url);
            assertThat(result).isEqualTo(url);
        }

        @Test
        void shouldAutoPrependHttps() {
            String result = service.validateAndNormalize("www.google.com");
            assertThat(result).isEqualTo("https://www.google.com");
        }
    }

    @Nested
    @DisplayName("invalid URLs")
    class InvalidUrls {

        @Test
        void shouldRejectNull() {
            assertThatThrownBy(() -> service.validateAndNormalize(null))
                .isInstanceOf(InvalidUrlException.class);
        }

        @Test
        void shouldRejectBlank() {
            assertThatThrownBy(() -> service.validateAndNormalize("   "))
                .isInstanceOf(InvalidUrlException.class);
        }

        @Test
        void shouldRejectFtpScheme() {
            assertThatThrownBy(() -> service.validateAndNormalize("ftp://files.example.com"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("HTTP and HTTPS");
        }

        @Test
        void shouldRejectJavascriptScheme() {
            assertThatThrownBy(() -> service.validateAndNormalize("javascript:alert(1)"))
                .isInstanceOf(InvalidUrlException.class);
        }

        @Test
        void shouldRejectLocalhost() {
            assertThatThrownBy(() -> service.validateAndNormalize("http://localhost:3000"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("not allowed");
        }

        @Test
        void shouldRejectPrivateIp() {
            assertThatThrownBy(() -> service.validateAndNormalize("http://192.168.1.1"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("private");
        }

        @Test
        void shouldRejectTooLongUrl() {
            String longUrl = "https://example.com/" + "a".repeat(2100);
            assertThatThrownBy(() -> service.validateAndNormalize(longUrl))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("maximum length");
        }
    }
}
