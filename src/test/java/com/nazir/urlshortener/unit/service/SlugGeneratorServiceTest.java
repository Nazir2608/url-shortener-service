package com.nazir.urlshortener.unit.service;

import com.nazir.urlshortener.config.AppProperties;
import com.nazir.urlshortener.config.AppProperties.CorsProperties;
import com.nazir.urlshortener.config.AppProperties.SlugProperties;
import com.nazir.urlshortener.exception.InvalidUrlException;
import com.nazir.urlshortener.exception.SlugAlreadyExistsException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import com.nazir.urlshortener.service.SlugGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlugGeneratorService")
class SlugGeneratorServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    private SlugGeneratorService slugGeneratorService;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
            "http://localhost:8080",
            new SlugProperties(7, 3, 20),
            new CorsProperties(List.of("http://localhost:3000"))
        );
        slugGeneratorService = new SlugGeneratorService(shortUrlRepository, props);
    }

    @Nested
    @DisplayName("generateUniqueSlug()")
    class GenerateUniqueSlug {

        @Test
        void shouldGenerateSlugOfConfiguredLength() {
            when(shortUrlRepository.existsBySlug(anyString())).thenReturn(false);

            String slug = slugGeneratorService.generateUniqueSlug();

            assertThat(slug).hasSize(7);
            assertThat(slug).matches("^[a-zA-Z0-9]+$");
        }

        @Test
        void shouldRetryOnCollision() {
            // First call collides, second succeeds
            when(shortUrlRepository.existsBySlug(anyString()))
                .thenReturn(true)
                .thenReturn(false);

            String slug = slugGeneratorService.generateUniqueSlug();

            assertThat(slug).isNotBlank();
        }
    }

    @Nested
    @DisplayName("validateCustomSlug()")
    class ValidateCustomSlug {

        @Test
        void shouldAcceptValidCustomSlug() {
            when(shortUrlRepository.existsBySlug("my-link")).thenReturn(false);

            String result = slugGeneratorService.validateCustomSlug("my-link");

            assertThat(result).isEqualTo("my-link");
        }

        @Test
        void shouldThrowForReservedSlug() {
            assertThatThrownBy(() -> slugGeneratorService.validateCustomSlug("admin"))
                .isInstanceOf(InvalidUrlException.class);
        }

        @Test
        void shouldThrowForExistingSlug() {
            when(shortUrlRepository.existsBySlug("taken")).thenReturn(true);

            assertThatThrownBy(() -> slugGeneratorService.validateCustomSlug("taken"))
                .isInstanceOf(SlugAlreadyExistsException.class);
        }

        @Test
        void shouldThrowForTooShortSlug() {
            assertThatThrownBy(() -> slugGeneratorService.validateCustomSlug("ab"))
                .isInstanceOf(InvalidUrlException.class);
        }
    }
}
